package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.BasePlayer;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_11_R1.boss.CraftBossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public abstract class AbstractWeapon implements Weapon {
    private BasePlayer owner;
    private CraftBossBar bar;
    private int bullets;

    public static AbstractWeapon createWeapon(Class<? extends AbstractWeapon> class_, PBPlayer owner) {
        try {
            AbstractWeapon w = class_.newInstance();
            w.setOwner(owner);
            return w;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected AbstractWeapon() {
        bar = new CraftBossBar("Ammo: " + bullets, BarColor.PURPLE, BarStyle.SOLID);
    }

    public void setBarColor() {
        ChatColor color = owner.getCurrentTeam().getColor();
        BarColor barColor;
        switch (color) {
            /*case BLACK:
            case DARK_GRAY:
                break;*/
            case BLUE:
            case AQUA:
            case DARK_BLUE:
            case DARK_AQUA:
                barColor = BarColor.BLUE;
                break;
            case GREEN:
            case DARK_GREEN:
                barColor = BarColor.GREEN;
                break;
            case RED:
            case DARK_RED:
                barColor = BarColor.RED;
                break;
            case DARK_PURPLE:
                barColor = BarColor.PURPLE;
                break;
            case YELLOW:
            case GOLD:
                barColor = BarColor.YELLOW;
                break;
            case LIGHT_PURPLE:
                barColor = BarColor.PINK;
                break;
            case WHITE:
            case GRAY:
                barColor = BarColor.WHITE;
                break;
            default:
                barColor = BarColor.BLUE; //TODO pick some other default color
                break;
        }
        bar.setColor(barColor);
        bar.setTitle(color + "Ammo: " + bullets);
    }

    public void refill() {
        bullets = clipSize();
        bar.setTitle((owner.getCurrentTeam() != null ? owner.getCurrentTeam().getColor() : "") + "Ammo: " + bullets);
        bar.setProgress(1.0);
    }

    public void removeBar() {
        bar.removeAll();
    }

    public void setOwner(BasePlayer owner) {
        this.owner = owner;
        if (owner instanceof PBPlayer)
            bar.addPlayer(((PBPlayer) owner).getBukkitPlayer());
    }

    private void updateGUI() {
        if (owner == null || !(owner instanceof PBPlayer))
            return;
        bar.setTitle(owner.getCurrentTeam().getColor() + "Ammo: " + bullets);
        bar.setProgress(clipSize() == 0 ? 0 : (1.0 * bullets / clipSize()));
    }

    @Override
    public void emptyGun() {
        bullets = 0;
        if (owner == null || !(owner instanceof PBPlayer))
            return;

        Inventory inventory = ((PBPlayer) owner).getBukkitPlayer().getInventory();
        for (ItemStack i : inventory) {
            int c = Weapon.WeaponUtils.getBulletCount(i);
            if (c == 0)
                continue;
            inventory.remove(i);
        }
        updateGUI();
    }

    private int getMaxBullets() {
        if (owner == null || !(owner instanceof PBPlayer))
            return 0;
        Inventory items = ((PBPlayer) owner).getBukkitPlayer().getInventory();
        int i = 0;
        for (ItemStack item : items)
            i += Weapon.WeaponUtils.getBulletCount(item);
        return i;
    }

    @Override
    public Material getMaterial() {
        if (owner == null || GameService.getCurrentGame() == null || getOwner().getCurrentTeam() == null)
            return getNormalMaterial();
        else {
            PaintballGame pg = GameService.getCurrentGame();
            if (pg.getConfig().getBlueTeam().equals(getOwner().getCurrentTeam()))
                return getBlueTeamMaterial();
            else
                return getRedTeamMaterial();
        }
    }

    @Override
    public void addBullets(int amount) {
        if (owner == null || !(owner instanceof PBPlayer))
            return;
        PBPlayer pOwner = ((PBPlayer) owner);
        if (pOwner.getBukkitPlayer().getInventory().firstEmpty() == -1) {
            combineBullets();
            combineBullets();
            combineBullets();
            if (pOwner.getBukkitPlayer().getInventory().firstEmpty() == -1)
                return;
        }
        if (amount <= clipSize()) {
            ItemStack item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), amount);
            pOwner.getBukkitPlayer().getInventory().addItem(item);
        } else {
            while (amount > 0) {
                int t;
                if (amount - clipSize() >= 0)
                    t = clipSize();
                else
                    t = amount;

                ItemStack item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), t);
                pOwner.getBukkitPlayer().getInventory().addItem(item);
                amount -= t;
            }
        }
        updateGUI();
    }

    private void combineBullets() {
        if (!(owner instanceof PBPlayer))
            return;
        final Inventory inventory = ((PBPlayer) owner).getBukkitPlayer().getInventory();
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

    private boolean reloading;

    @Override
    public void reload(ItemStack item) {
        if (owner == null || !(owner instanceof PBPlayer))
            return;
        PBPlayer pOwner = ((PBPlayer) owner);
        if (!reloading) {
            int c = Weapon.WeaponUtils.getBulletCount(item);
            if (c == 0)
                return;

            int bNeeded = clipSize() - bullets;
            if (bNeeded == 0) {
                pOwner.sendMessage(ChatColor.DARK_RED + "Reload failed! Your gun is full!");
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
                    pOwner.getBukkitPlayer().getInventory().clear(pOwner.getBukkitPlayer().getInventory().getHeldItemSlot());
                    final Inventory i = pOwner.getBukkitPlayer().getInventory();
                    while (true) {
                        int first_index = i.first(getReloadItem());
                        if (first_index == -1) {
                            reloading = false;
                            pOwner.sendMessage(ChatColor.DARK_RED + "You're all out!");
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
            pOwner.getBukkitPlayer().sendMessage(Paintball.formatMessage("Reloading..."));
            pOwner.getBukkitPlayer().playSound(pOwner.getBukkitPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1);
            displayReloadAnimation(reloadTime);

            Runnable stopReload = () -> {
                reloading = false;
                pOwner.getBukkitPlayer().sendMessage(Paintball.formatMessage(ChatColor.GREEN + "Reloaded!"));
                updateGUI();
            };
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, stopReload, (long) Math.round(reloadTime * 20));
        }
    }

    private void displayReloadAnimation(float speed) {
        if (owner == null || !(owner instanceof PBPlayer))
            return;
        bar.setTitle(owner.getCurrentTeam().getColor() + "Reloading");
        bar.setProgress(0);
        final float percentage = 1 / speed;
        for (int i = 1; i <= speed; i++)
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, () -> bar.setProgress(Math.min(1.0, bar.getProgress() + percentage)), (long) 20 * i);
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
        if (owner == null || !(owner instanceof PBPlayer))
            return;
        if (reloading) {
            ((PBPlayer) owner).sendMessage(ChatColor.DARK_RED + "You cant shoot while reloading!");
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
    public BasePlayer getOwner() {
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
        if (GameService.getCurrentGame() == null || !GameService.getCurrentGame().hasStarted())
            return;
        Team t = GameService.getCurrentGame().getTeamForPlayer(PBPlayer.getPlayer(bukkitPlayer));
        GameService.getCurrentGame().getScoreManager().addUUIDToTeam(t.getName(), snowball.getUniqueId());
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
        if (owner == null || !(owner instanceof PBPlayer))
            return;
        called = true;
        Player bukkitPlayer = ((PBPlayer) owner).getBukkitPlayer();
        bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 0.5f, 1);
        onFire(bukkitPlayer.getWorld().spawn(bukkitPlayer.getEyeLocation().clone(), Snowball.class), bukkitPlayer, spread);
    }
}