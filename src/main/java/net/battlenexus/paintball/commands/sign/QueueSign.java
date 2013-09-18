package net.battlenexus.paintball.commands.sign;

import net.battlenexus.paintball.Paintball;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

public class QueueSign extends SignStat {
    public QueueSign(Sign block) {
        super(block);
    }

    int oldvalue = -1;
    @Override
    public void tick() {
        if (Paintball.INSTANCE.getGameService().getQueueCount() != oldvalue) {
            int temp = Paintball.INSTANCE.getGameService().getQueueCount();
            updateSign("Next Game:", getText(temp), "Map: " + Paintball.INSTANCE.getGameService().getMapName());
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
}
