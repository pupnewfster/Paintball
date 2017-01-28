package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public abstract class AbstractWeapon implements Weapon {
    private PBPlayer owner;
    private int bullets;
    protected int currentClip;

    public static AbstractWeapon createWeapon(Class<? extends AbstractWeapon> class_, PBPlayer owner) {
        try {
            AbstractWeapon w = class_.newInstance();
            w.owner = owner;
            return w;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected AbstractWeapon() {
    }

    private void updateGUI() {
        if (owner == null)
            return;
        getOwner().getBukkitPlayer().setLevel(bullets);
        getOwner().getBukkitPlayer().setExp(clipSize() == 0 ? 0 : (float) (1.0 * bullets / clipSize()));
    }

    @Override
    public void emptyGun() {
        bullets = 0;
        if (owner == null)
            return;

        Inventory inventory = owner.getBukkitPlayer().getInventory();
        for (ItemStack i : inventory) {
            int c = Weapon.WeaponUtils.getBulletCount(i);
            if (c == 0)
                continue;
            inventory.remove(i);
        }
        updateGUI();
    }

    private int getMaxBullets() {
        if (owner == null)
            return 0;
        Inventory items = owner.getBukkitPlayer().getInventory();
        int i = 0;
        for (ItemStack item : items)
            i += Weapon.WeaponUtils.getBulletCount(item);
        return i;
    }

    @Override
    public Material getMaterial() {
        if (owner == null || getOwner().getCurrentGame() == null || getOwner().getCurrentTeam() == null)
            return getNormalMaterial();
        else {
            PaintballGame pg = getOwner().getCurrentGame();
            if (pg.getConfig().getBlueTeam().equals(getOwner().getCurrentTeam()))
                return getBlueTeamMaterial();
            else
                return getRedTeamMaterial();
        }
    }

    @Override
    public void addBullets(int amount) {
        if (owner == null)
            return;
        if (owner.getBukkitPlayer().getInventory().firstEmpty() == -1) {
            combineBullets();
            combineBullets();
            combineBullets();
            if (owner.getBukkitPlayer().getInventory().firstEmpty() == -1)
                return;
        }
        if (amount <= clipSize()) {
            ItemStack item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), amount);
            owner.getBukkitPlayer().getInventory().addItem(item);
        } else {
            while (amount > 0) {
                int t;
                if (amount - clipSize() >= 0)
                    t = clipSize();
                else
                    t = amount;

                ItemStack item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), t);
                owner.getBukkitPlayer().getInventory().addItem(item);
                amount -= t;
            }
        }
        updateGUI();
    }

    private void combineBullets() {
        final Inventory inventory = owner.getBukkitPlayer().getInventory();
        int i = inventory.first(getReloadItem());
        ItemStack item = inventory.getItem(i);
        int bulletCount = Weapon.WeaponUtils.getBulletCount(item);
        inventory.setItem(i, null);

        i = inventory.first(getReloadItem());
        if (i == -1) {
            inventory.addItem(item);
            return;
        }
        item = inventory.getItem(i);
        int bulletCount2 = Weapon.WeaponUtils.getBulletCount(item);
        inventory.setItem(i, null);

        int newAmount = bulletCount + bulletCount2;
        item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), newAmount);
        inventory.addItem(item);
    }

    protected abstract Material getBlueTeamMaterial();

    protected abstract Material getRedTeamMaterial();

    protected abstract Material getNormalMaterial();

    private boolean reloading = false;

    @Override
    public void reload(ItemStack item) {
        if (owner == null)
            return;
        if (!reloading) {
            int c = Weapon.WeaponUtils.getBulletCount(item);
            if (c == 0)
                return;

            int bNeeded = clipSize() - bullets;
            if (bNeeded == 0) {
                owner.sendMessage(ChatColor.DARK_RED + "Reload failed! Your gun is full!");
                return;
            }
            int take = 0;
            float reloadTime = (bNeeded / clipSize()) * reloadDelay();
            if (bNeeded == c)
                take = c;
            else if (bNeeded < c)
                take = bNeeded;
            else if (bNeeded > c)
                take = c;
            bullets += take;
            reloading = true;
            if (c - take > 0)
                Weapon.WeaponUtils.setBulletCount(item, c - take);
            else {
                if (item.getAmount() > 1)
                    item.setAmount(item.getAmount() - 1);
                else {
                    owner.getBukkitPlayer().getInventory().clear(owner.getBukkitPlayer().getInventory().getHeldItemSlot());
                    final Inventory i = owner.getBukkitPlayer().getInventory();
                    while (true) {
                        int first_index = i.first(getReloadItem());
                        if (first_index == -1) {
                            reloading = false;
                            owner.sendMessage(ChatColor.DARK_RED + "You're all out!");
                            break;
                        }
                        ItemStack item_to_move = i.getItem(first_index);
                        if (!item_to_move.hasItemMeta() || !item_to_move.getItemMeta().hasDisplayName() || !item_to_move.getItemMeta().hasLore()) {
                            i.setItem(first_index, null);
                            continue;
                        }
                        i.clear(first_index);
                        int clear = i.firstEmpty();
                        i.setItem(clear, item_to_move);
                        break;
                    }
                }
            }
            updateGUI();
            owner.getBukkitPlayer().sendMessage(Paintball.formatMessage("Reloading..."));
            owner.getBukkitPlayer().playSound(owner.getBukkitPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1);
            displayReloadAnimation(reloadTime);

            Runnable stopReload = () -> {
                reloading = false;
                owner.getBukkitPlayer().sendMessage(Paintball.formatMessage(ChatColor.GREEN + "Reloaded!"));
                updateGUI();
            };
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, stopReload, (long) Math.round(reloadTime * 20));
        }
    }

    private void displayReloadAnimation(float speed) {
        if (owner == null)
            return;
        owner.getBukkitPlayer().setExp(0);
        final Player thePlayer = owner.getBukkitPlayer();
        final float percentage = 1 / speed;
        for (int i = 1; i <= speed; i++)
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, () -> thePlayer.setExp(thePlayer.getExp() + percentage), (long) 20 * i);
    }

    @Override
    public void shoot() {
        shoot(0);
    }

    @Override
    public int currentClipSize() {
        return bullets;
    }

    @Override
    public int totalBullets() {
        return getMaxBullets();
    }

    private long lastFire = -1;

    protected void shoot(double spreadFactor) {
        if (owner == null)
            return;
        if (reloading) {
            owner.sendMessage(ChatColor.DARK_RED + "You cant shoot while reloading!");
            return;
        }
        if (bullets < getShotRate())
            return;

        if (lastFire == -1 || (System.currentTimeMillis() - lastFire) >= getFireDelay()) {
            int fire = getShotRate();
            bullets -= fire;

            updateGUI();

            called = false;
            while (fire > 0) {
                onShoot(spreadFactor);
                if (!called)
                    throw new RuntimeException("super.onShoot was not called! Try putting super.onShoot at the top of your method!");
                fire--;
            }
            lastFire = System.currentTimeMillis();
        }
    }

    @Override
    public PBPlayer getOwner() {
        return owner;
    }

    private boolean onehit;

    @Override
    public void setOneHitKill(boolean value) {
        this.onehit = value;
    }

    @Override
    public boolean isOneHitKill() {
        return onehit;
    }

    private boolean called;

    private void onFire(final Snowball snowball, Player bukkitPlayer, double spread) {
        if (bukkitPlayer == null)
            return;
        Team t = Paintball.INSTANCE.getGameService().getGame().getTeamForPlayer(PBPlayer.getPlayer(bukkitPlayer));
        Paintball.INSTANCE.getGameService().getGame().getScoreManager().addUUIDToTeam(t.getName(), snowball.getUniqueId());
        snowball.setGlowing(true);
        snowball.setShooter(bukkitPlayer);
        Vector vector;
        if (spread == 0)
            vector = bukkitPlayer.getEyeLocation().getDirection().multiply(strength());
        else {
            final Random random = new Random();
            Location pLoc = bukkitPlayer.getEyeLocation();
            double dir = -pLoc.getYaw() - 90;
            double pitch = -pLoc.getPitch();
            double xWep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
            //double yWep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
            double zWep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
            double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + xWep;
            double yd = Math.sin(Math.toRadians(pitch));
            double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + zWep;
            vector = new Vector(xd, yd, zd).multiply(strength());
        }
        snowball.setVelocity(vector);

        for (int i = 1; i <= 10; i++)
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, () -> snowball.getWorld().playEffect(snowball.getLocation(), Effect.SMOKE, 10), 2L * i);
    }

    private void onShoot(double spread) {
        if (owner == null)
            return;
        called = true;
        Player bukkitPlayer = owner.getBukkitPlayer();
        bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 0.5f, 1);
        onFire(bukkitPlayer.getWorld().spawn(bukkitPlayer.getEyeLocation().clone(), Snowball.class), bukkitPlayer, spread);
    }
}