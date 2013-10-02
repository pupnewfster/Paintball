package net.battlenexus.paintball.system.commands.sign;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.system.commands.JoinQueue;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class QueueSign extends SignStat implements Listener {
    public QueueSign(Sign block) {
        super(block);
        Paintball.INSTANCE.getServer().getPluginManager().registerEvents(this, Paintball.INSTANCE);
    }

    int oldvalue = -1;
    @Override
    public void tick() {
        if (Paintball.INSTANCE.getGameService().getQueueCount() != oldvalue) {
            int temp = Paintball.INSTANCE.getGameService().getQueueCount();
            updateSign("Next Game:", getText(temp), Paintball.INSTANCE.getGameService().getMapName());
            oldvalue = temp;
        }
    }

    private String getText(int temp) {
        int max = Paintball.INSTANCE.getGameService().getMaxPlayers();
        if (temp < max) {
            if (temp / 4 < max) {
                return "" + ChatColor.BOLD + temp + "/" + Paintball.INSTANCE.getGameService().getMaxPlayers();
            } else if (temp / 2 < max) {
                return "" + ChatColor.BOLD + ChatColor.YELLOW + temp + "/" + Paintball.INSTANCE.getGameService().getMaxPlayers();
            } else if (temp / 2 > max) {
                return "" + ChatColor.BOLD + ChatColor.DARK_RED + temp + "/" + Paintball.INSTANCE.getGameService().getMaxPlayers();
            } else {
                return "" + ChatColor.BOLD + temp + "/" + Paintball.INSTANCE.getGameService().getMaxPlayers();
            }
        } else {
            return "" + ChatColor.BOLD + ChatColor.DARK_RED + "FULL";
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;
        Player player = event.getPlayer();
        PBPlayer pbPlayer = PBPlayer.toPBPlayer(player);
        if (event.getClickedBlock().getLocation().equals(getSignBlock().getLocation())) { //This is our block!
            new JoinQueue().executePlayer(pbPlayer, new String[0]);
        }
    }
}
