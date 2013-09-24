package net.battlenexus.paintball.game.impl;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class OneHitMinute extends PaintballGame {
    private int seconds = 60;
    private boolean setup;
    private static final String TIME_LEFT_NAME = "Time Left";
    private static final OfflinePlayer TIME_LEFT = Bukkit.getOfflinePlayer(TIME_LEFT_NAME);
    private static final OfflinePlayer[] TIME_LEFT_ARRAY = new OfflinePlayer[] { TIME_LEFT };
    private int bscore;
    private int rscore;

    @Override
    protected void onGameStart() {
        sendGameMessage(ChatColor.DARK_RED + "ONE HIT MINUTE!");
        showScore();
    }

    @Override
    public void onPlayerJoin(PBPlayer player) {
        super.onPlayerJoin(player);
        if (!setup) {
            super.setupScoreboard();
            setup = true;
        }
        OfflinePlayer[] thisPlayer = new OfflinePlayer[1];
        thisPlayer[0] = Bukkit.getOfflinePlayer(player.getBukkitPlayer().getName());
        score.addPlayersToTeam(player.getCurrentTeam().getName(), thisPlayer);
        player.getCurrentWeapon().setOneHitKill(true);
    }

    @Override
    public void onPlayerLeave(PBPlayer player) {

        player.getCurrentWeapon().setOneHitKill(false);
    }

    @Override
    protected void setupScoreboard() {
        score.addTeam(TIME_LEFT_NAME, TIME_LEFT_ARRAY);
        score.addPoints(TIME_LEFT, seconds);
        super.setupScoreboard();
    }

    @Override
    public void onPlayerKill(PBPlayer killer, PBPlayer victim) {
        super.onPlayerKill(killer, victim);
        if (killer != null && killer.getCurrentTeam() != null) {
            Team t = killer.getCurrentTeam();
            if (t == super.getConfig().getBlueTeam()) {
                bscore++;
                score.addPoints(Bukkit.getOfflinePlayer(super.getConfig().getBlueTeam().getName()), 1);
            } else {
                rscore++;
                score.addPoints(Bukkit.getOfflinePlayer(super.getConfig().getRedTeam().getName()), 1);
            }
        } else if (killer == null) {
            if (victim.getCurrentTeam() != null) {
                Team t = victim.getCurrentTeam();
                if (t == super.getConfig().getBlueTeam()) {
                    rscore++;
                } else {
                    bscore++;
                }
            }
        }
    }

    @Override
    public void tick() {
        seconds--;
        score.addPoints(TIME_LEFT, -1);
    }

    @Override
    public int getTimeout() {
        return 1000;
    }
}
