package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public interface Weapon {
    public String name();

    public int clipeSize();

    public int startBullets();

    public int strength();

    public int reloadDelay();

    public int getShotRate();

    public Material getMaterial();

    public Material getReloadItem();

    public PBPlayer getOwner();

    public void shoot();

    public int currentBullets();

    public void reload(ItemStack item);

    public class WeaponUtils {
        public static ItemStack toItemStack(Weapon weapon) {
            ItemStack itemStack = new ItemStack(weapon.getMaterial());

            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(weapon.name());
            List<String> lore = new ArrayList<String>();
            lore.add("Clip Size: " + weapon.clipeSize());
            lore.add("Strength: " + weapon.strength());
            lore.add("Fire Rate: " + weapon.getShotRate());
            im.setLore(lore);
            itemStack.setItemMeta(im);
            return itemStack;
        }

        public static ItemStack createReloadItem(Material material, int amount) {
            ItemStack itemStack = new ItemStack(material);

            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.GREEN + "Ammo Clip");
            List<String> lore = new ArrayList<String>();
            lore.add("Bullet Count: " + amount);
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
            if (!m.getDisplayName().equals(ChatColor.GREEN + "Ammo Clip"))
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
    }
}
