package net.battlenexus.paintball.game.items.impl;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.items.AbstractItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Health extends AbstractItem {

    @Override
    public Material getMaterial() {
        return Material.EMERALD;
    }

    @Override
    public String getName() {
        return "Increased Health";
    }

    //Options for amplifier should be more than 0
    @Override
    public void addEffect(PBPlayer p, ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if(!is.getItemMeta().hasLore()) {
            return;
        }
        p.getBukkitPlayer().getInventory().remove(is);
        List<String> lore = im.getLore();
        int amount = Integer.parseInt(lore.get(1).split(" ")[1]);
        p.increasMaxHealth(amount);
    }
}
