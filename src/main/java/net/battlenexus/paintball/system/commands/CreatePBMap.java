package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.config.MapConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;
import java.util.HashMap;

public class CreatePBMap implements PBCommand, Listener {
    private final HashMap<Player, CreationHelper> creators = new HashMap<>();

    public CreatePBMap() {
        Paintball.INSTANCE.getServer().getPluginManager().registerEvents(this, Paintball.INSTANCE);
    }

    @Override
    public void executePlayer(PBPlayer player, String[] args) {
        if (creators.containsKey(player.getBukkitPlayer())) {
            creators.remove(player.getBukkitPlayer());
            player.getBukkitPlayer().sendMessage(Paintball.formatMessage("Map creation canceled.."));
            return;
        }

        creators.put(player.getBukkitPlayer(), new CreationHelper());
        player.getBukkitPlayer().sendMessage(Paintball.formatMessage("Welcome to the paintball Map Creator!"));
        player.getBukkitPlayer().sendMessage(Paintball.formatMessage("Why don't we start off by telling me the name of this map."));
        player.getBukkitPlayer().sendMessage(Paintball.formatMessage("So, whats the name?"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("This command can only be used in game!");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (creators.containsKey(event.getPlayer())) {
            CreationHelper ch = creators.get(event.getPlayer());
            if (ch.step == 2) {
                event.setCancelled(true);
                Block b = event.getBlock();
                if (b.getType() == Material.CHEST) {
                    Location l = b.getLocation();
                    ch.mapConfig.addChest(l);
                    event.getPlayer().sendMessage(Paintball.formatMessage(ChatColor.GREEN + "Chest added!"));
                } else {
                    event.getPlayer().sendMessage(Paintball.formatMessage(ChatColor.RED + "That's not a chest!"));
                    event.getPlayer().sendMessage(Paintball.formatMessage(ChatColor.RED + "If your done with this step, say \"pineapple\""));
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (creators.containsKey(event.getPlayer())) {
            CreationHelper ch = creators.get(event.getPlayer());
            if (ch.step == 0) {
                String mapName = event.getMessage();
                event.setCancelled(true);
                ch.step++;
                ch.mapConfig.setMapName(mapName);
                event.getPlayer().sendMessage(Paintball.formatMessage("So this map is called \"" + mapName + "\", awesome!"));
                event.getPlayer().sendMessage(Paintball.formatMessage("How many players is this map for? (1000 for no limit)"));
            } else if (ch.step == 1) {
                String text = event.getMessage();
                int number;
                try {
                    number = Integer.parseInt(text);
                } catch (Throwable t) {
                    return;
                }
                if (number <= 1) {
                    event.getPlayer().sendMessage(Paintball.formatMessage("The number of players must be greater than 1!"));
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                ch.mapConfig.setPlayerMax(number);
                ch.step++;
                event.getPlayer().sendMessage(Paintball.formatMessage("So this map is for " + (number >= 1000 ? "everyone!" : number + " players!")));
                event.getPlayer().sendMessage(Paintball.formatMessage("Ok. Next, hit all chests in this map"));
                event.getPlayer().sendMessage(Paintball.formatMessage("Once you've hit all the chests, say \"pineapple\""));
            } else if (ch.step == 2) {
                if (event.getMessage().equalsIgnoreCase("pineapple")) {
                    event.setCancelled(true);
                    ch.step++;
                    event.getPlayer().sendMessage(Paintball.formatMessage("Ok. Next step!"));
                    event.getPlayer().sendMessage(Paintball.formatMessage("Whats the name of the first team?"));
                }
            } else if (ch.step == 3) {
                String blue = event.getMessage();
                event.setCancelled(true);
                ch.step++;
                ch.mapConfig.setTeamName(0, blue);
                event.getPlayer().sendMessage(Paintball.formatMessage("Ok."));
                event.getPlayer().sendMessage(Paintball.formatMessage("Whats the name of the second team?"));
            } else if (ch.step == 4) {
                String red = event.getMessage();
                event.setCancelled(true);
                ch.step++;
                ch.mapConfig.setTeamName(1, red);
                event.getPlayer().sendMessage(Paintball.formatMessage("Awesome!"));
                event.getPlayer().sendMessage(Paintball.formatMessage("Please stand on the spawn for the team \"" + ch.mapConfig.getBlueTeam().getName() + "\""));
                event.getPlayer().sendMessage(Paintball.formatMessage("Then say \"set\""));
            } else if (ch.step == 5) {
                if (event.getMessage().toLowerCase().equals("set")) {
                    Location spawn = event.getPlayer().getLocation();
                    event.setCancelled(true);
                    ch.step++;
                    ch.mapConfig.addSpawn(0, spawn, true, true);
                    event.getPlayer().sendMessage(Paintball.formatMessage("Awesome!"));
                    event.getPlayer().sendMessage(Paintball.formatMessage("Please stand on the spawn for the team \"" + ch.mapConfig.getRedTeam().getName() + "\""));
                    event.getPlayer().sendMessage(Paintball.formatMessage("Then say \"set\""));
                }
            } else if (ch.step == 6) {
                if (event.getMessage().toLowerCase().equals("set")) {
                    Location spawn = event.getPlayer().getLocation();
                    event.setCancelled(true);
                    ch.step++;
                    ch.mapConfig.addSpawn(1, spawn, true, true);
                    event.getPlayer().sendMessage(Paintball.formatMessage("Awesome!"));
                    event.getPlayer().sendMessage(Paintball.formatMessage("Please wait while I save this to a map file.."));

                    try {
                        ch.mapConfig.saveToFile(ch.mapConfig.getMapName().toLowerCase() + ".xml");
                        event.getPlayer().sendMessage(Paintball.formatMessage(ChatColor.GREEN + "All set!"));
                        event.getPlayer().sendMessage(Paintball.formatMessage("Thanks for using the Map Creator!"));
                        event.getPlayer().sendMessage(Paintball.formatMessage("Goodbye."));
                        creators.remove(event.getPlayer());
                    } catch (IOException e) {
                        e.printStackTrace();
                        event.getPlayer().sendMessage(Paintball.formatMessage(ChatColor.DARK_RED + "Sorry, there was an error saving your map :/"));
                    }
                }
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{
                "createpbmap",
                "cpbmap"
        };
    }

    public class CreationHelper {
        public final MapConfig mapConfig = new MapConfig();
        public int step;
    }
}