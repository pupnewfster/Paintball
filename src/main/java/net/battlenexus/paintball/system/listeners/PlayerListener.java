package net.battlenexus.paintball.system.listeners;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.items.AbstractItem;
import net.battlenexus.paintball.system.inventory.PaintballMenu;
import net.battlenexus.paintball.system.inventory.impl.WeaponShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class PlayerListener implements Listener {
    private final HashMap<String, String> deathMessages = new HashMap<>();

    public PlayerListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PBPlayer pbPlayer = PBPlayer.toPBPlayer(event.getPlayer());
        event.getPlayer().teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation());
        pbPlayer.clearInventory();
        event.getPlayer().getInventory().setMaxStackSize(1); //TODO is this really needed anymore?
        event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setHealth(20.0);
        pbPlayer.showLobbyItems();
        Paintball.makePlayerGhost(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null)
            return;
        //Removes from queue if in queue when leaving
        Paintball.INSTANCE.getGameService().leaveQueue(who);
        //TODO leave game if in a game
        who.dispose();
        Paintball.makePlayerVisible(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta() != null &&
                    p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Weapon Shop")) {
                WeaponShopMenu menu = new WeaponShopMenu(ChatColor.BOLD + "Weapon Shop");
                menu.displayInventory(p);
                event.setCancelled(true);
                return;
            }
        }

        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null)
            return;
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().equals(Material.CHEST) &&
                !event.getClickedBlock().getType().equals(Material.TRAPPED_CHEST))) {
            //GUN
            if (who.getCurrentWeapon() != null && p.getInventory().getItemInMainHand().getType().equals(who.getCurrentWeapon().getMaterial())) {
                who.getCurrentWeapon().shoot();
                event.setCancelled(true);
            }
            //RELOAD
            else if (who.getCurrentWeapon() != null && p.getInventory().getItemInMainHand().getType().equals(who.getCurrentWeapon().getReloadItem())) {
                who.getCurrentWeapon().reload(p.getInventory().getItemInMainHand());
                event.setCancelled(true);
            }
            AbstractItem item = AbstractItem.getItem(p.getInventory().getItemInMainHand().getType());
            //POWERUP
            if (item != null && who.isInGame()) {
                item.addEffect(who, p.getInventory().getItemInMainHand());
                event.setCancelled(true);
            }
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType().equals(Material.ANVIL)))
            event.setCancelled(true);
    }

    @EventHandler
    public void inventoryClicked(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            Inventory i = event.getInventory();
            if (i.getHolder() != null && i.getHolder() instanceof PaintballMenu) {
                PaintballMenu menu = (PaintballMenu) event.getInventory().getHolder();
                menu.onItemClicked(event);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PBPlayer pbvictim;
            if (!(event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) && !(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                if ((pbvictim = PBPlayer.getPlayer(victim)) == null)
                    return;
                if (pbvictim.isInGame() || pbvictim.isSpectating())
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Snowball) || !(event.getEntity() instanceof Player) || !(((Projectile) event.getDamager()).getShooter() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        PBPlayer victim;
        PBPlayer shooter = PBPlayer.toPBPlayer((Player) ((Snowball) event.getDamager()).getShooter());

        if ((victim = PBPlayer.getPlayer((Player) event.getEntity())) == null)
            return;

        victim.hit(shooter);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String playerName = event.getEntity().getName();
        if (deathMessages.containsKey(playerName)) {
            event.setDeathMessage(deathMessages.get(playerName));
            deathMessages.remove(playerName);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        PBPlayer player;
        if ((player = PBPlayer.getPlayer(event.getPlayer())) != null)
            if (player.isFrozen() && (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()))
                player.handleFrozen();
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        //Part of custom "adventure mode" to stop breaking of blocks
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null)
            return;
        //Stop them if they are in game
        if (who.isInGame() || who.isSpectating())
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //Part of custom "adventure mode" to stop placing of blocks
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null)
            return;
        //Stop them if they are in game
        if (who.isInGame() || who.isSpectating())
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null)
            return;
        //Stops them from dropping items if they are inGame
        if (who.isInGame() || who.isSpectating())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null)
            return;
        if (who.isInGame()) {
            String team = "(" + ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR, who.getCurrentTeam().getName()) + ChatColor.RESET + ") ";
            event.setFormat(team + event.getFormat());
        } else if (who.isSpectating()) {
            who.sendMessage("You are spectating, you cannot talk!"); //TODO Maybe only send messages to other spectators
            event.setCancelled(true);
        }
    }
}