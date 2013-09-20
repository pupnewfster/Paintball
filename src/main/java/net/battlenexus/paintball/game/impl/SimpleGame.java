package net.battlenexus.paintball.game.impl;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class SimpleGame extends PaintballGame implements Listener {
    private int rscore, bscore;

    @Override
    public void tick() {
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    protected void onGameStart() {
        sendGameMessage("First team to 20 kills win!");
    }

    @Override
    public void onPlayerKill(PBPlayer killer, PBPlayer victim) {
        super.onPlayerKill(killer, victim);
        if (killer.getCurrentTeam() != null) {
            Team t = killer.getCurrentTeam();
            if (t == super.getConfig().getBlueTeam()) {
                bscore++;
            } else {
                rscore++;
            }

            if (bscore >= 20) {
                sendGameMessage("The " + getConfig().getBlueTeam().getName() + ChatColor.GRAY + " team wins!");
                super.endGame();
            } else if (rscore >= 20) {
                sendGameMessage("The " + getConfig().getRedTeam().getName() + ChatColor.GRAY + " team wins!");
                super.endGame();
            }
        }
    }
}
