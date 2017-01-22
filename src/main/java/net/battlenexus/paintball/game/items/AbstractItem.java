package net.battlenexus.paintball.game.items;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.items.impl.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItem {
    private static final Class<?>[] ITEMS = new Class[]{
            Speed.class,
            Health.class,
            Food.class
    };

    public abstract Material getMaterial();

    public abstract String getName();

    public abstract void addEffect(PBPlayer p, ItemStack is);

    public abstract boolean canGoInChest();

    public abstract String durationMessage(); //Only used if there is not a timer

    public abstract boolean hasTimer();

    public abstract boolean hasAmplifier();

    protected AbstractItem() {
    }

    //For when we add item to their inventory so we get ItemStack
    public static ItemStack createItem(Material material, int duration, int amplifier) {
        AbstractItem item = getItem(material);
        if (item == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack itemStack = new ItemStack(material);
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName(item.getName());
        List<String> lore = new ArrayList<String>();
        String dur;
        if (item.hasTimer()) {
            int temp = duration % 60;
            dur = Integer.toString(duration / 60) + ":" + (temp == 0 ? "00" : (temp < 10 ? "0" + Integer.toString(temp) : Integer.toString(temp)));
        } else {
            dur = item.durationMessage();
        }
        lore.add("Duration: " + dur);
        if (item.hasAmplifier()) {
            lore.add("Amplifier: " + amplifier);
        }
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return itemStack;
    }

    public static AbstractItem getItem(Material mat) {
        List<AbstractItem> items = getItems();
        for (AbstractItem i : items) {
            if (i.getMaterial().equals(mat))
                return i;
        }

        return null;
    }

    public static ArrayList<AbstractItem> getItems() {
        ArrayList<AbstractItem> items = new ArrayList<AbstractItem>();
        for (Class<?> class_ : ITEMS) {
            try {
                items.add((AbstractItem) class_.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return items;
    }

    public static ArrayList<AbstractItem> getChestItems() {
        ArrayList<AbstractItem> items = new ArrayList<AbstractItem>();
        for (Class<?> class_ : ITEMS) {
            try {
                if (((AbstractItem) class_.newInstance()).canGoInChest()) {
                    items.add((AbstractItem) class_.newInstance());
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return items;
    }
}
