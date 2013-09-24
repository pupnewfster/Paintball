package net.battlenexus.paintball.system.commands.sign;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.system.commands.PBCommand;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PlaceSignCommand implements PBCommand, Listener {
    private HashMap<Player, Class<?>> queue = new HashMap<Player, Class<?>>();

    public PlaceSignCommand() {
        Paintball.INSTANCE.getServer().getPluginManager().registerEvents(this, Paintball.INSTANCE);
    }
    @Override
    public void executePlayer(PBPlayer player, String[] args) {
        if (!player.getBukkitPlayer().isOp()) {
            player.sendMessage("You cannot use this command!");
            return;
        }
        if (queue.containsKey(player.getBukkitPlayer())) {
            queue.remove(player.getBukkitPlayer());
            player.sendMessage("Action canceled..");
            return;
        }
        if (args.length != 1) {
            player.sendMessage("Usage: /sign <type>");
        } else {
            String type = args[0];
            try {
                Class<? extends SignStat> _class = (Class<? extends SignStat>) Class.forName("net.battlenexus.paintball.system.commands.sign." + type);
                player.sendMessage("Please hit the sign to attach this stat to...");
                queue.put(player.getBukkitPlayer(), _class);
            } catch (ClassNotFoundException e) {
                player.sendMessage("Sign type not found!");
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("You must be in-game to use this command!");
    }

    @Override
    public String[] getNames() {
        return new String[] {
                "signstat"
        };
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (queue.containsKey(p)) {
            Block b = event.getBlock();
            if (b.getType() != Material.SIGN && b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST)
                return;
            Sign s = (Sign)b.getState();
            event.setCancelled(true);
            try {
                SignStat stat = (SignStat) queue.get(p).getConstructor(Sign.class).newInstance(s);
                SignStat.addStat(stat);
            } catch (InstantiationException e) {
                e.printStackTrace();
                p.sendMessage("There was an error creating the Sign!");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                p.sendMessage("There was an error creating the Sign!");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                p.sendMessage("There was an error creating the Sign!");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                p.sendMessage("There was an error creating the Sign!");
            }
            queue.remove(p);
        }
    }


}
