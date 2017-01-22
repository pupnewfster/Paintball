package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface Weapon {
    void setOneHitKill(boolean value);

    boolean isOneHitKill();

    String name();

    int clipSize();

    int startBullets();

    int damage();

    float strength();

    int reloadDelay();

    int getShotRate();

    int getFireDelay();

    Material getMaterial();

    Material getReloadItem();

    PBPlayer getOwner();

    void shoot();

    void emptyGun();

    int currentClipSize();

    int totalBullets();

    void addBullets(int bullets);

    void reload(ItemStack item);

    class WeaponUtils {
        public static ItemStack toItemStack(Weapon weapon) {
            ItemStack itemStack = new ItemStack(weapon.getMaterial());

            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(weapon.name());
            List<String> lore = new ArrayList<>();
            lore.add("Clip Size: " + weapon.clipSize());
            lore.add("Strength: " + weapon.damage());
            lore.add("Fire Rate: " + weapon.getShotRate());
            im.setLore(lore);
            itemStack.setItemMeta(im);
            return itemStack;
        }

        private static final Random random = new Random();

        public static ItemStack createReloadItem(Material material, int amount) {
            ItemStack itemStack = new ItemStack(material);

            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.GREEN + "Ammo Clip (" + amount + ")");
            List<String> lore = new ArrayList<>();
            lore.add("Bullet Count: " + amount);
            long id = random.nextLong();
            lore.add("ID: " + id); //TODO Find a better way to make these not stack
            im.setLore(lore);
            itemStack.setItemMeta(im);

            return itemStack;
        }

        public static int getBulletCount(ItemStack item) {
            if (item == null)
                return 0;
            ItemMeta m = item.getItemMeta();
            if (m == null)
                return 0;
            if (m.getDisplayName() == null || !m.getDisplayName().contains(ChatColor.GREEN + "Ammo Clip"))
                return 0;
            List<String> lore_list = m.getLore();
            if (lore_list == null || lore_list.size() < 1)
                return 0;
            String lore = lore_list.get(0);
            try {
                return Integer.parseInt(lore.split(":")[1].trim());
            } catch (Throwable t) {
                return 0;
            }
        }

        public static void setBulletCount(ItemStack item, int amount) {
            if (item == null)
                return;
            ItemMeta m = item.getItemMeta();
            m.setDisplayName(ChatColor.GREEN + "Ammo Clip (" + amount + ")");
            List<String> lore;
            if (!m.hasLore()) {
                lore = new ArrayList<>();
                lore.add("Bullet Count: " + amount);
                lore.add("ID: " + random.nextLong()); //TODO Find a better way to make these not stack
            } else {
                lore = m.getLore();
                lore.set(0, "Bullet Count: " + amount);
            }
            m.setLore(lore);
            item.setItemMeta(m);
        }
    }
}