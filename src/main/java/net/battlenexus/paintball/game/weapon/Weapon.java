package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.entities.PBPlayer;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
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
            im.setLore(Collections.singletonList("Bullet Count: " + amount));
            itemStack.setItemMeta(im);

            net.minecraft.server.v1_11_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = null;
            if (nmsStack.hasTag())
                tag = nmsStack.getTag();
            else
                nmsStack.setTag(tag = new NBTTagCompound());
            tag.setInt("HideFlags", 63);
            tag.setLong("ID", random.nextLong());
            nmsStack.setTag(tag);
            return CraftItemStack.asCraftMirror(nmsStack);
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
            m.setLore(Collections.singletonList("Bullet Count: " + amount));
            item.setItemMeta(m);
        }
    }
}