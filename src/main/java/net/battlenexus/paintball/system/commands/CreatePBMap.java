package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;
import java.util.HashMap;

public class CreatePBMap implements PBCommand, Listener {
    private HashMap<Player, CreationHelper> creators = new HashMap<Player, CreationHelper>();

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
        player.getBukkitPlayer().sendMessage(Paintball.formatMessage("Why dont we start off by telling me the name of this map."));
        player.getBukkitPlayer().sendMessage(Paintball.formatMessage("So, whats the name?"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("This command can only be used ingame!");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (creators.containsKey(event.getPlayer())) {
            CreationHelper ch = creators.get(event.getPlayer());
            if (ch.step == 0) {
                String mapname = event.getMessage();
                event.setCancelled(true);
                ch.step++;
                ch.config.setMapName(mapname);
                event.getPlayer().sendMessage(Paintball.formatMessage("So this map is called \"" + mapname + "\", awesome!"));
                event.getPlayer().sendMessage(Paintball.formatMessage("How many players is this map for? (1000 for no limit)"));
            } else if (ch.step == 1) {
                String text = event.getMessage();
                int number = 0;
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
                ch.config.setPlayerMax(number);
                ch.step++;
                event.getPlayer().sendMessage(Paintball.formatMessage("So this map is for " + (number >= 1000 ? "everyone!" : number + " players!")));
                event.getPlayer().sendMessage(Paintball.formatMessage("Ok. Whats the name of the first team?"));
            } else if (ch.step == 2) {
                String blue = event.getMessage();
                event.setCancelled(true);
                ch.step++;
                ch.config.setTeamName(0, blue);
                event.getPlayer().sendMessage(Paintball.formatMessage("Ok."));
                event.getPlayer().sendMessage(Paintball.formatMessage("Whats the name of the second team?"));
            } else if (ch.step == 3) {
                String red = event.getMessage();
                event.setCancelled(true);
                ch.step++;
                ch.config.setTeamName(1, red);
                event.getPlayer().sendMessage(Paintball.formatMessage("Awesome!"));
                event.getPlayer().sendMessage(Paintball.formatMessage("Please stand on the spawn for the team \"" + ch.config.getBlueTeam().getName() + "\""));
                event.getPlayer().sendMessage(Paintball.formatMessage("Then say \"set\""));
            } else if (ch.step == 4) {
                if (event.getMessage().toLowerCase().equals("set")) {
                    Location spawn = event.getPlayer().getLocation();
                    event.setCancelled(true);
                    ch.step++;
                    ch.config.setTeamSpawn(0, spawn);
                    event.getPlayer().sendMessage(Paintball.formatMessage("Awesome!"));
                    event.getPlayer().sendMessage(Paintball.formatMessage("Please stand on the spawn for the team \"" + ch.config.getRedTeam().getName() + "\""));
                    event.getPlayer().sendMessage(Paintball.formatMessage("Then say \"set\""));
                }
            } else if (ch.step == 5) {
                if (event.getMessage().toLowerCase().equals("set")) {
                    Location spawn = event.getPlayer().getLocation();
                    event.setCancelled(true);
                    ch.step++;
                    ch.config.setTeamSpawn(1, spawn);
                    event.getPlayer().sendMessage(Paintball.formatMessage("Awesome!"));
                    event.getPlayer().sendMessage(Paintball.formatMessage("Please wait while I save this to a map file.."));

                    try {
                        ch.config.saveToFile(ch.config.getMapName().toLowerCase());
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
        return new String[] {
                "createpbmap",
                "cpbmap"
        };
    }

    public class CreationHelper {
        public Config config = new Config();
        public int step = 0;
    }
}
