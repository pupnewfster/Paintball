package net.battlenexus.paintball.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    public PlayerListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //Kill everyone, blame it on the new guy.
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player)) {
            return;
        }

        Player killer = (Player) event.getDamager();

        killer.damage(20D);

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerProjectileHit(ProjectileHitEvent event) {
        if(!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player killer = (Player) event.getEntity().getShooter();
        killer.damage(20D);
    }

}