package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

public abstract class AbstractWeapon implements Weapon {
    private PBPlayer owner;
    protected ArrayList<Clip> clips = new ArrayList<Clip>();
    protected int currentClip;

    public static AbstractWeapon createWeapon(Class<? extends AbstractWeapon> class_, PBPlayer owner) {
        try {
            AbstractWeapon w =  class_.newInstance();
            w.owner = owner;
            w.addBullets(w.startBullets());
            return w;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected AbstractWeapon() {
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
        updateGUI();
    }

    protected void updateGUI() {
        getOwner().getBukkitPlayer().setExp(clipCount());
        getOwner().getBukkitPlayer().setLevel(clips.get(currentClip).bullets);
    }

    @Override
    public Material getMaterial() {
        if (getOwner().getCurrentGame() == null || getOwner().getCurrentTeam() == null)
            return getNormalMaterial();
        else {
            PaintballGame pg = getOwner().getCurrentGame();
            if (pg.getConfig().getBlueTeam().equals(getOwner().getCurrentTeam())) {
                return getBlueTeamMaterial();
            } else {
                return getRedTeamMaterial();
            }
        }
    }

    public abstract Material getBlueTeamMaterial();

    public abstract Material getRedTeamMaterial();

    public abstract Material getNormalMaterial();

    private boolean reloading = false;
    @Override
    public void reload() {
        if (!reloading) {
            int bneeded = clipeSize() - currentClipSize();
            final int clipcount = clipCount();
            if (clipcount <= 0) {
                //TODO No more bullets!
                updateGUI();
            }
            else if (clipcount < bneeded) {
                bneeded = clipcount;
            }

            float reloadtime = (bneeded / clipeSize()) * reloadDelay();
            reloading = true;
            int obtain = obtainFromOtherClips(bneeded);
            if (clips.size() == 0) {
                Clip c = new Clip();
                c.bullets = obtain;
            } else {
                Clip c = clips.get(currentClip);
                c.bullets += obtain;
            }
            if (obtain == 0) {
                owner.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + Paintball.formatMessage("Your all out!"));
                return;
            }
            updateGUI();
            owner.getBukkitPlayer().sendMessage(Paintball.formatMessage("Reloading..."));
            displayReloadAnimation(reloadtime);

            Runnable stopReload = new Runnable() {

                @Override
                public void run() {
                    reloading = false;
                    owner.getBukkitPlayer().sendMessage(Paintball.formatMessage(ChatColor.GREEN + "Reloaded!"));
                    updateGUI();
                }
            };
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, stopReload, (long) Math.round(reloadtime * 20));
        }
    }

    private void displayReloadAnimation(float speed) {
        owner.getBukkitPlayer().setExp(0);
        final Player thePlayer = owner.getBukkitPlayer();
        final int finalspeed = Math.round((speed / 10) * 20);
        Runnable fixTask = new Runnable() {

            @Override
            public void run() {
                thePlayer.setExp((float) (thePlayer.getExp() + 0.1));
            }
        };
        for (int i = 1; i <= 10; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, fixTask, (long) finalspeed * i);
        }
    }

    @Override
    public void shoot() {
        shoot(0);
    }

    @Override
    public int currentClipSize() {
        if (clips.size() == 0 || currentClip == -1) {
            return 0;
        }

        Clip c = clips.get(currentClip);
        return c.count();
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
        updateGUI();

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

    protected int obtainFromOtherClips(int request) {
        int temp = 0;
        for (int i = 0; i < clips.size(); i++) {
            if (i == currentClip)
                continue;
            if (temp >= request)
                break;
            Clip c = clips.get(i);
            if (c.count() >= request) {
                c.remove(request);
                return request;
            } else if (c.count() > 0 && c.count() < request) {
                int gain = request - temp;
                if (c.count() < gain) {
                    gain = c.count();
                }
                temp += gain;
                c.remove(gain);
            }
        }
        return temp;
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
