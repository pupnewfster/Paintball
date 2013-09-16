package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

public abstract class AbstractWeapon implements Weapon {
    private PBPlayer owner;
    protected ArrayList<Clip> clips = new ArrayList<Clip>();
    protected int currentClip;

    public AbstractWeapon createWeapon(Class<? extends AbstractWeapon> class_, PBPlayer owner) {
        try {
            return class_.getConstructor(PBPlayer.class).newInstance(owner);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected AbstractWeapon(PBPlayer player) {
        this.owner = player;
    }

    public void addBullets(int bulletCount) {
        final int max = clipeSize() == 0 ? 1 : clipeSize();
        int i = 0;
        while (bulletCount > 0) {
            if (i >= clips.size()) {
                clips.add(new Clip());
            }

            final Clip c = clips.get(i);
            if (c.count() < max) {
                c.bullets++;
                bulletCount--;
            } else {
                i++;
            }
        }
    }

    public void shoot() {
        shoot(0);
    }

    public void shoot(double spreadfactor) {
        if (clips.size() == 0 || currentClip == -1) {
            return;
        }

        Clip c = clips.get(currentClip);
        if (c.count() < getShotRate()) {
            return;
        }

        c.remove(getShotRate());
        getOwner().getBukkitPlayer().setExp(clipCount());
        getOwner().getBukkitPlayer().setLevel(c.bullets);

        called = false;
        onShoot(spreadfactor);
        if (!called)
            throw new RuntimeException("super.onShoot was not called! Try putting super.onShoot at the top of your method!");
    }

    protected void findNextClip() {
        currentClip = -1;
        for (int i = 0; i < clips.size(); i++) {
            if (clips.get(i).count() > 0) {
                currentClip = i;
                break;
            }
        }
    }

    protected int clipCount() {
        int temp = 0;
        for (Clip clip : clips) {
            if (clip.count() > 0) {
                temp++;
            }
        }
        return temp;
    }

    @Override
    public PBPlayer getOwner() {
        return owner;
    }

    private boolean called;
    protected void onShoot(double spread) {
        called = true;
        Player bukkitPlayer = owner.getBukkitPlayer();
        bukkitPlayer.playEffect(bukkitPlayer.getLocation(), Effect.CLICK1, 10);

        final Snowball snowball = bukkitPlayer.getWorld().spawn(bukkitPlayer.getEyeLocation(), Snowball.class);
        snowball.setShooter(bukkitPlayer);
        snowball.setTicksLived(2400);
        Vector vector;
        if (spread == 0) {
            vector = bukkitPlayer.getLocation().getDirection().multiply(strength());
        } else {
            final Random random = new Random();
            Location ploc = bukkitPlayer.getLocation();
            double dir = -ploc.getYaw() - 90;
            double pitch = -ploc.getPitch();
            double xwep = ((random.nextInt((int)(spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
            double ywep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
            double zwep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
            double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + xwep;
            double yd = Math.sin(Math.toRadians(pitch)) + ywep;
            double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + zwep;
            vector = new Vector(xd, yd, zd).multiply(strength());
        }
        snowball.setVelocity(vector);

        Runnable task = new Runnable() {

            @Override
            public void run() {
                Location loc = snowball.getLocation();
                for (int a = 1; a <= 2; a++) {
                    snowball.getWorld().playEffect(loc, Effect.SMOKE, 10);
                }
            }
        };
        for (int i = 1; i <= 10; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, task, 2L * i);
        }
    }

    protected class Clip {
        public int bullets;

        public void remove(int count) {
            if (bullets - count < 0)
                throw new InvalidParameterException("Requesting more bullets than there is!");
            bullets -= count;
        }

        public int count() {
            return bullets;
        }
    }
}
