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

    public int currentClipSize();

    public void reload();

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
            itemStack.setItemMeta(im); //TODO I dont think this is needed..
            return itemStack;
        }

        public static ItemStack createReloadItem(Weapon weapon) {
            ItemStack itemStack = new ItemStack(weapon.getReloadItem());

            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.GREEN + "Reload");

            itemStack.setItemMeta(im); //TODO I dont think this is needed..
            return itemStack;
        }
    }
}
