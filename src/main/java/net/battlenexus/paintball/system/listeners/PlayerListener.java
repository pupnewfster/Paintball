package net.battlenexus.paintball.system.listeners;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.BasePlayer;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.ai.SimpleSkeleton;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.game.items.AbstractItem;
import net.battlenexus.paintball.system.inventory.PaintballMenu;
import net.battlenexus.paintball.system.inventory.impl.WeaponShopMenu;
import net.minecraft.server.v1_11_R1.EntitySkeletonAbstract;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftSkeleton;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;

public class PlayerListener implements Listener {
    private final HashMap<String, String> deathMessages = new HashMap<>();

    public PlayerListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        PBPlayer pbPlayer = PBPlayer.toPBPlayer(p);
        p.teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation());
        pbPlayer.clearInventory();
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.setHealth(20.0);
        p.setGameMode(GameMode.ADVENTURE);
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
        who.dispose();
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        EquipmentSlot hand = event.getHand();
        ItemStack item = hand.equals(EquipmentSlot.HAND) ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();
        //TODO: if there for some reason become more than two hands check if it is offhand and then check for third hand
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().getDisplayName().equals("Weapon Shop")) {
                WeaponShopMenu menu = new WeaponShopMenu(ChatColor.BOLD + "Weapon Shop");
                menu.displayInventory(p);
                event.setCancelled(true);
                return;
            }
        }

        PBPlayer who = PBPlayer.getPlayer(p);
        if (who == null || GameService.getCurrentGame() == null || !GameService.getCurrentGame().hasStarted())
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
            else if (event.getSlotType().equals(InventoryType.SlotType.ARMOR) || (i.getType().equals(InventoryType.CRAFTING) && event.getRawSlot() == 45))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getInventory().getType().equals(InventoryType.CRAFTING) && event.getRawSlots().contains(45))
            event.setCancelled(true);
    }

    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
            event.setCancelled(true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball))
            return;
        ProjectileSource ps = event.getEntity().getShooter();
        //TODO check to make sure it is a player
        BasePlayer shooter = ps instanceof Player ? PBPlayer.toPBPlayer((Player) ps) : (SimpleSkeleton) ((CraftSkeleton) ps).getHandle(); //TODO write a better check of if simple skeleton
        if (shooter == null)
            return; //not sure how this is possible, but does it need to be handled with removing snowball from having glow
        if (event.getHitEntity() != null) {
            if (event.getHitEntity() instanceof Player) {
                PBPlayer victim = PBPlayer.getPlayer((Player) event.getHitEntity());
                if (victim == null)
                    return;
                victim.hit(shooter);
            } else {
                Entity e = event.getHitEntity();
                if (e.getType().equals(EntityType.SKELETON)) { //TODO
                    EntitySkeletonAbstract esa = ((CraftSkeleton) e).getHandle();
                    if (esa instanceof SimpleSkeleton) {
                        SimpleSkeleton simple = (SimpleSkeleton) esa;
                        simple.hit(shooter);
                    }
                }//TODO should we handle when it hits a different entity
            }
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
    public void onEntityBowFire(EntityShootBowEvent event) {
        if (event.getEntity().getType().equals(EntityType.SKELETON)) {
            Projectile p = (Projectile) event.getProjectile();

            Snowball snow = event.getEntity().getWorld().spawn(event.getEntity().getEyeLocation().clone(), Snowball.class);
            snow.setVelocity(p.getVelocity());
            snow.setShooter(p.getShooter());
            if (GameService.getCurrentGame() != null) {
                EntitySkeletonAbstract esa = ((CraftSkeleton) event.getEntity()).getHandle();
                if (esa instanceof SimpleSkeleton) {
                    GameService.getCurrentGame().getScoreManager().addUUIDToTeam(((SimpleSkeleton) esa).getCurrentTeam().getName(), snow.getUniqueId());
                    snow.setGlowing(true);
                }
            }
            p.remove();
            //TODO shoot based on their type of bow with different strength/spread
        }//TODO support other types maybe?
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PBPlayer who = PBPlayer.getPlayer(event.getPlayer());
        if (who != null && who.isSpectating()) { //TODO: Add a chat for spectators
            who.sendMessage("You are spectating, you cannot talk!"); //TODO Maybe only send messages to other spectators
            event.setCancelled(true);
        }
    }
}