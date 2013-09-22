package net.battlenexus.paintball.listeners;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class PlayerListener implements Listener {

    protected HashMap<String, String> deathMessages = new HashMap<String, String>();

    public PlayerListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation());
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().getInventory().clear();
        event.getPlayer().getInventory().setMaxStackSize(1);
        event.getPlayer().setMaxHealth(20.0);
        event.getPlayer().setHealth(20.0);

        Paintball.getGhostManager().addPlayer(event.getPlayer());
        //PBPlayer.newPlayer(event.getPlayer()); This really isnt need here..
        //event.setJoinMessage("The faggot " + event.getPlayer().getDisplayName() + " has joined the game");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null) {
            return;
        }
        //Removes from queue if in queue when leaving
        Paintball.INSTANCE.getGameService().leaveQueue(who);
        //TODO leave game if in a game
        Paintball.getGhostManager().removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null) {
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            //GUN
            if (who.getCurrentWeapon() != null && p.getInventory().getItemInHand().getType().equals(who.getCurrentWeapon().getMaterial())) {
                who.getCurrentWeapon().shoot();
            }
            //RELOAD
            if (who.getCurrentWeapon() != null && p.getInventory().getItemInHand().getType().equals(who.getCurrentWeapon().getReloadItem())) {
                who.getCurrentWeapon().reload(p.getInventory().getItemInHand());
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PBPlayer pbvictim;
            if(!(event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) && !(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                if ((pbvictim = PBPlayer.getPlayer(victim)) == null) {
                    return;
                }
                if (pbvictim.isInGame() || pbvictim.isSpectating()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Snowball) || !(event.getEntity() instanceof Player) || !(((Projectile) event.getDamager()).getShooter() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        PBPlayer victim;
        PBPlayer shooter = PBPlayer.toPBPlayer((Player) ((Snowball)event.getDamager()).getShooter());

        if ((victim = PBPlayer.getPlayer((Player) event.getEntity())) == null) {
            return;
        }

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
        if ((player = PBPlayer.getPlayer(event.getPlayer())) != null) {
            if (player.isFrozen() && (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()))
                player.handleFrozen();
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        //Part of custom "adventure mode" to stop breaking of blocks
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null) {
            return;
        }
        //Stop them if they are ingame
        if(who.isInGame() || who.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //Part of custom "adventure mode" to stop placing of blocks
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null) {
            return;
        }
        //Stop them if they are ingame
        if(who.isInGame() || who.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null) {
            return;
        }
        //Stops them from dropping items if they are inGame
        if(who.isInGame() || who.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null) {
            return;
        }
        if(who.isInGame()) {
            String team = "(" + ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR, who.getCurrentTeam().getName()) + ChatColor.RESET + ") ";
            event.setFormat(team + event.getFormat());
        } else if (who.isSpectating()) {
            who.sendMessage("You are spectating, you cannot talk!"); //TODO Maybe only send messages to other spectators
            event.setCancelled(true);
        }
    }
}