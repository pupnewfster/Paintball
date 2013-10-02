package net.battlenexus.paintball.game.items;

import net.battlenexus.paintball.game.items.impl.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItem {
    public abstract Material getMaterial();
    public abstract String getName();
    public abstract void addEffect(PBPlayer p, ItemStack is);

    protected AbstractItem() {
    }

    //For when we add item to their inventory so we get ItemStack
    public static ItemStack createItem(Material material, int duration, int amplifier) {
        AbstractItem item = getItem(material);
        if(item == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack itemStack = new ItemStack(material);
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName(item.getName());
        List<String> lore = new ArrayList<String>();
        String dur = Integer.toString(duration / 60) + ":" + Integer.toString(duration % 60);
        lore.add("Duration: " + dur);
        lore.add("Amplifier: " + amplifier);
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return itemStack;
    }

    public static AbstractItem getItem(Material mat) {
        if((new Speed()).getMaterial().equals(mat))
            return new Speed();
        else if((new Health()).getMaterial().equals(mat))
            return new Health();
        return null;
    }
}
