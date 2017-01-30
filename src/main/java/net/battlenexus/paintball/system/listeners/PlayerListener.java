package net.battlenexus.paintball.system.listeners;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.game.items.AbstractItem;
import net.battlenexus.paintball.system.inventory.PaintballMenu;
import net.battlenexus.paintball.system.inventory.impl.WeaponShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
        event.getPlayer().setSaturation(20);
        event.getPlayer().setHealth(20.0);
        pbPlayer.showLobbyItems();
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
        event.getPlayer().setGameMode(GameMode.SURVIVAL);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        EquipmentSlot hand = event.getHand();
        ItemStack item;
        if (hand.equals(EquipmentSlot.HAND))
            item = p.getInventory().getItemInMainHand();
        else //TODO: if there for some reason become more than two hands check if it is offhand and then check for third hand
            item = p.getInventory().getItemInOffHand();

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().getDisplayName().equals("Weapon Shop")) {
                WeaponShopMenu menu = new WeaponShopMenu(ChatColor.BOLD + "Weapon Shop");
                menu.displayInventory(p);
                event.setCancelled(true);
                return;
            }
        }

        PBPlayer who = PBPlayer.getPlayer(p);
        if (who == null)
            return;
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().equals(Material.CHEST) &&
                !event.getClickedBlock().getType().equals(Material.TRAPPED_CHEST))) {
            //GUN
            if (who.getCurrentWeapon() != null && item.getType().equals(who.getCurrentWeapon().getMaterial())) {
                who.getCurrentWeapon().shoot();
                event.setCancelled(true);
            }
            //RELOAD
            else if (who.getCurrentWeapon() != null && item.getType().equals(who.getCurrentWeapon().getReloadItem())) {
                who.getCurrentWeapon().reload(item);
                event.setCancelled(true);
            }
            AbstractItem abstractItem = AbstractItem.getItem(item.getType());
            //POWERUP
            if (abstractItem != null && who.isInGame()) {
                abstractItem.addEffect(who, item);
                event.setCancelled(true);
            }
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType().equals(Material.ANVIL)))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClicked(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Inventory i = event.getInventory();
            if (i.getHolder() != null && i.getHolder() instanceof PaintballMenu)
                ((PaintballMenu) event.getInventory().getHolder()).onItemClicked(event);
            else if (event.getSlotType().equals(InventoryType.SlotType.ARMOR) || event.getRawSlot() == 45) //TODO: If we have chests or things with 45 slots do not just cancel
                event.setCancelled(true); //TODO fix disabling offhand slot
        }
    }

    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            PBPlayer pbvictim = PBPlayer.getPlayer((Player) event.getEntity());
            if (pbvictim == null)
                return;
            if (pbvictim.isInGame() || pbvictim.isSpectating())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball))
            return;
        PBPlayer shooter = PBPlayer.toPBPlayer((Player) event.getEntity().getShooter());
        if (shooter == null)
            return; //not sure how this is possible, but does it need to be handled with removing snowball from having glow
        if (event.getHitEntity() != null) {
            if (event.getHitEntity() instanceof Player) {
                PBPlayer victim = PBPlayer.getPlayer((Player) event.getHitEntity());
                if (victim == null)
                    return;
                victim.hit(shooter);
            } //TODO should we handle when it hits a different entity
        }
        if (shooter.getCurrentTeam() != null) //TODO what do we do if their team is null
            GameService.getCurrentGame().getScoreManager().removeUUIDFromTeam(shooter.getCurrentTeam().getName(), event.getEntity().getUniqueId());
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