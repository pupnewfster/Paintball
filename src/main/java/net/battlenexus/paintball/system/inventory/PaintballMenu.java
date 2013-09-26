package net.battlenexus.paintball.system.inventory;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

public abstract class PaintballMenu implements InventoryHolder {
    private Inventory inventory;

    public PaintballMenu() {
        inventory = Bukkit.createInventory(this, slotCount(), getName());
        addItems(inventory);
    }

    public PaintballMenu(String menu) {
        inventory = Bukkit.createInventory(this, slotCount(), menu);
        addItems(inventory);
    }

    public void displayInventory(PBPlayer p) {
        displayInventory(p.getBukkitPlayer());
    }

    public void displayInventory(Player p) {
        openInventory(p, inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public abstract String getName();

    public abstract int slotCount();

    public abstract void addItems(Inventory i);

    public abstract void onItemClicked(InventoryClickEvent event);

    protected void closeInventory(final HumanEntity entity) {
        runNextTick(new Runnable() {

            @Override
            public void run() {
                entity.closeInventory();
            }
        });
    }

    protected void openInventory(final HumanEntity entity, final Inventory i) {
        runNextTick(new Runnable() {

            @Override
            public void run() {
                entity.openInventory(i);
            }
        });
    }

    protected void openWorkbench(final HumanEntity entity, final Location loc, final boolean value) {
        runNextTick(new Runnable() {

            @Override
            public void run() {
                entity.openWorkbench(loc, value);
            }
        });
    }

    protected void openEnchanting(final HumanEntity entity, final Location loc, final boolean value) {
        runNextTick(new Runnable() {

            @Override
            public void run() {
                entity.openEnchanting(loc, value);
            }
        });
    }

    protected void close(final InventoryView view) {
        runNextTick(new Runnable() {

            @Override
            public void run() {
                view.close();
            }
        });
    }

    protected void runNextTick(Runnable runnable) {
        Bukkit.getScheduler().runTask(Paintball.INSTANCE, runnable);
    }
}
