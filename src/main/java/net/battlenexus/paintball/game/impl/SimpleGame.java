package net.battlenexus.paintball.game.impl;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class SimpleGame extends PaintballGame implements Listener {
    private int rscore, bscore;
    private boolean didsetup;

    @Override
    public void tick() {
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public String getGamemodeName() {
        return "Basic Paintball";
    }

    @Override
    protected void onGameStart() {
        sendGameMessage("First team to 20 kills win!");
    }

    @Override
    public void onPlayerJoin(PBPlayer player) {
        super.onPlayerJoin(player);
        if (!didsetup) {
            super.setupScoreboard();
            didsetup = true;
        }
        score.addPlayerToTeam(player.getCurrentTeam().getName(), player.getBukkitPlayer());
    }

    public void onPlayerLeave(PBPlayer player) {
        super.onPlayerLeave(player);
        if (!didsetup) {
            super.setupScoreboard();
            didsetup = true;
        }
        score.removePlayerFromTeam(player.getCurrentTeam().getName(), player.getBukkitPlayer());
    }

    @Override
    public void onPlayerKill(PBPlayer killer, PBPlayer victim) {
        super.onPlayerKill(killer, victim);
        if (killer != null && killer.getCurrentTeam() != null) {
            Team t = killer.getCurrentTeam();
            if (t == super.getConfig().getBlueTeam()) {
                bscore++;
                score.addPoints(super.getConfig().getBlueTeam().getName(), 1);
            } else {
                rscore++;
                score.addPoints(super.getConfig().getRedTeam().getName(), 1);
            }
        } else if (killer == null) {
            if (victim.getCurrentTeam() != null) {
                Team t = victim.getCurrentTeam();
                if (t == super.getConfig().getBlueTeam())
                    rscore++;
                else
                    bscore++;
            }
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