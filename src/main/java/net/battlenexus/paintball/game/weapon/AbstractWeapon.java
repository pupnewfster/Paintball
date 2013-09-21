package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public abstract class AbstractWeapon implements Weapon {
    private PBPlayer owner;
    private int bullets;
    protected int currentClip;

    public static AbstractWeapon createWeapon(Class<? extends AbstractWeapon> class_, PBPlayer owner) {
        try {
            AbstractWeapon w =  class_.newInstance();
            w.owner = owner;
            w.bullets += w.startBullets();
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

    protected void updateGUI() {
        getOwner().getBukkitPlayer().setExp((getMaxBullets() == 0 ? 0 : bullets / getMaxBullets()));
        getOwner().getBukkitPlayer().setLevel(bullets);
    }

    public int getMaxBullets() {
        ItemStack[] items = owner.getBukkitPlayer().getInventory().getContents();
        int i = 0;
        for (ItemStack item : items) {
            i += Weapon.WeaponUtils.getBulletCount(item);
        }
        return i;
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
    public void reload(ItemStack item) {
        if (!reloading) {
            int c = Weapon.WeaponUtils.getBulletCount(item);
            if (c == 0)
                return;

            int bneeded = bullets - clipeSize();
            float reloadtime = (bneeded / clipeSize()) * reloadDelay();
            bullets += c;
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
    public int currentBullets() {
        return bullets;
    }

    public void shoot(double spreadfactor) {
        if (reloading) {
            owner.sendMessage(ChatColor.DARK_RED + "You cant shoot while reloading!");
        }
        if (bullets < getShotRate()) {
            return;
        }

        bullets -= getShotRate();

        updateGUI();

        called = false;
        onShoot(spreadfactor);
        if (!called)
            throw new RuntimeException("super.onShoot was not called! Try putting super.onShoot at the top of your method!");
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
}
