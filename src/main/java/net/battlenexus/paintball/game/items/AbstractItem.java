package net.battlenexus.paintball.game.items;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.items.impl.Food;
import net.battlenexus.paintball.game.items.impl.Health;
import net.battlenexus.paintball.game.items.impl.Speed;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
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

    protected abstract String getName();

    public abstract void addEffect(PBPlayer p, ItemStack is);

    protected abstract boolean canGoInChest();

    protected abstract String durationMessage(); //Only used if there is not a timer

    protected abstract boolean hasTimer();

    protected abstract boolean hasAmplifier();

    protected AbstractItem() {
    }

    //For when we add item to their inventory so we get ItemStack
    public static ItemStack createItem(Material material, int duration, int amplifier) {
        AbstractItem item = getItem(material);
        if (item == null)
            return new ItemStack(Material.AIR);
        ItemStack itemStack = new ItemStack(material);
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName(item.getName());
        List<String> lore = new ArrayList<>();
        String dur;
        if (item.hasTimer()) {
            int temp = duration % 60;
            dur = Integer.toString(duration / 60) + ":" + (temp == 0 ? "00" : (temp < 10 ? "0" + Integer.toString(temp) : Integer.toString(temp)));
        } else
            dur = item.durationMessage();
        lore.add("Duration: " + dur);
        if (item.hasAmplifier())
            lore.add("Amplifier: " + amplifier);
        im.setLore(lore);
        itemStack.setItemMeta(im);
        return itemStack;
    }

    public static AbstractItem getItem(Material mat) {
        return getItems().stream().filter(i -> i.getMaterial().equals(mat)).findFirst().orElse(null);
    }

    private static ArrayList<AbstractItem> getItems() {
        ArrayList<AbstractItem> items = new ArrayList<>();
        for (Class<?> class_ : ITEMS)
            try {
                items.add((AbstractItem) class_.newInstance());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        return items;
    }

    public static ArrayList<AbstractItem> getChestItems() {
        ArrayList<AbstractItem> items = new ArrayList<>();
        for (Class<?> class_ : ITEMS) {
            try {
                if (((AbstractItem) class_.newInstance()).canGoInChest())
                    items.add((AbstractItem) class_.newInstance());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return items;
    }

    @SuppressWarnings("ConstantConditions")
    public static ItemStack addGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        net.minecraft.server.v1_11_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        if (!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        if (tag == null)
            tag = nmsStack.getTag();
        tag.setInt("HideFlags", 63);
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
    }
}