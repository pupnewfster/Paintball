package net.battlenexus.paintball.system.inventory.impl;

import com.crossge.necessities.Economy.BalChecks;
import com.crossge.necessities.Necessities;
import com.crossge.necessities.Variables;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import net.battlenexus.paintball.game.weapon.Weapon;
import net.battlenexus.paintball.game.weapon.impl.MachineGun;
import net.battlenexus.paintball.game.weapon.impl.Pistol;
import net.battlenexus.paintball.game.weapon.impl.Shotgun;
import net.battlenexus.paintball.game.weapon.impl.Sniper;
import net.battlenexus.paintball.system.inventory.PaintballMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WeaponShopMenu extends PaintballMenu {
    private static final Class<?>[] WEAPONS = new Class[]{ //MUST BE A MULTIPLE OF 9!
            Pistol.class, MachineGun.class, Sniper.class, Shotgun.class, null, null, null, null, null
    };
    private static final int[] PRICES = new int[]{
            0, 10, 10, 10, 0, 0, 0, 0, 0
    };

    public WeaponShopMenu(String s) {
        super(s);
    }

    @Override
    public String getName() {
        return ChatColor.BOLD + "Weapon Shop";
    }

    @Override
    public int slotCount() {
        return WEAPONS.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addItems(Inventory inventory) {
        for (int i = 0; i < WEAPONS.length; i++) {
            if (WEAPONS[i] == null)
                continue;
            ItemStack item = toItemStack((Class<? extends Weapon>) WEAPONS[i]); //GOD DAMN WEAK TYPING
            if (item == null)
                continue;
            ItemMeta meta = item.getItemMeta();
            List<String> lore;
            if (meta.hasLore())
                lore = meta.getLore();
            else
                lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "Price: " + PRICES[i]);
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClicked(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final Player p = (Player) event.getWhoClicked();

            final int slot = event.getSlot();
            if (slot < 0 || slot >= WEAPONS.length)
                return;

            event.setCancelled(true);
            final PBPlayer player = PBPlayer.toPBPlayer(p);

            int price = PRICES[slot];

            BalChecks b = Necessities.getInstance().getBalChecks();
            if (b.balance(p.getUniqueId()) < price) {
                Variables var = Necessities.getInstance().getVar();
                p.sendMessage(var.getEr() + "Error: " + var.getErMsg() + "You do not have " + price + " GGs."); //TODO when BalChecks is changed to Economy using mysql use the format
                event.setCancelled(true);
                return;
            }

            //Prevent any form of error..
            runNextTick(() -> {
                p.getInventory().clear();
                if (!player.isInGame())
                    player.showLobbyItems();
                player.setWeapon(AbstractWeapon.createWeapon((Class<? extends AbstractWeapon>) WEAPONS[slot], player)); //dude...that's weak..
                p.closeInventory();
                b.removeMoney(p.getUniqueId(), price);
                wakeUp();
            });
        }
    }

    private ItemStack toItemStack(Class<? extends Weapon> class_) {
        try {
            Weapon w = class_.newInstance();
            return Weapon.WeaponUtils.toItemStack(w);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}