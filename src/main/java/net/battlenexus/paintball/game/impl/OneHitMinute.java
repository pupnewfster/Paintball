package net.battlenexus.paintball.game.impl;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.PaintballGame;
import net.battlenexus.paintball.game.weapon.Weapon;
import net.battlenexus.paintball.game.weapon.impl.Pistol;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.List;

public class OneHitMinute extends PaintballGame {
    private int seconds = 60;
    private boolean setup;
    private static final String TIME_LEFT_NAME = "Time";
    private int bscore;
    private int rscore;

    @Override
    public String getGamemodeName() {
        return "One Hit Minute";
    }

    @Override
    protected List<Class<? extends Weapon>> allowedGuns() {
        return Collections.singletonList(Pistol.class);
    }

    @Override
    protected void onGameStart() {
        sendGameMessage(ChatColor.DARK_RED + "ONE HIT MINUTE!");
    }

    @Override
    public void onPlayerJoin(PBPlayer player) {
        super.onPlayerJoin(player);
        if (!setup) {
            setupScoreboard();
            setup = true;
        }
        score.addPlayerToTeam(player.getCurrentTeam().getName(), player.getBukkitPlayer());
        player.getCurrentWeapon().setOneHitKill(true);
    }

    @Override
    public void onPlayerLeave(PBPlayer player) {
        super.onPlayerLeave(player);
        player.getCurrentWeapon().setOneHitKill(true);
        if (!setup) {
            setupScoreboard();
            setup = true;
        }
        score.removePlayerFromTeam(player.getCurrentTeam().getName(), player.getBukkitPlayer());
    }

    @Override
    protected void setupScoreboard() {
        score.addTeam(TIME_LEFT_NAME);
        score.addPoints(TIME_LEFT_NAME, seconds);
        super.setupScoreboard();
    }

    @Override
    public void onPlayerKill(PBPlayer killer, PBPlayer victim) {
        super.onPlayerKill(killer, victim);
        if (killer != null && killer.getCurrentTeam() != null) {
            Team t = killer.getCurrentTeam();
            if (t == super.getConfig().getBlueTeam()) {
                bscore++;
                score.addPoints(super.getConfig().getBlueTeam().getName(), 1);
                if (sd) {
                    sendGameMessage("The " + getConfig().getBlueTeam().getName() + ChatColor.GRAY + " team wins!");
                    super.endGame();
                }
            } else {
                rscore++;
                score.addPoints(super.getConfig().getRedTeam().getName(), 1);
                if (sd) {
                    sendGameMessage("The " + getConfig().getRedTeam().getName() + ChatColor.GRAY + " team wins!");
                    super.endGame();
                }
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
    }

    private boolean sd;

    @Override
    public void tick() {
        if (!started)
            return;
        seconds--;
        if (seconds <= 0) {
            if (!sd) {
                sendGameMessage(ChatColor.DARK_RED + "The game has ended!");
                if (bscore > rscore) {
                    sendGameMessage("The " + getConfig().getBlueTeam().getName() + ChatColor.GRAY + " team wins!");
                    super.endGame();
                } else if (rscore > bscore) {
                    sendGameMessage("The " + getConfig().getRedTeam().getName() + ChatColor.GRAY + " team wins!");
                    super.endGame();
                } else {
                    sendGameMessage(ChatColor.DARK_RED + "SUDDEN DEATH");
                    sendGameMessage("Next kill in 30 seconds wins!");
                    seconds = 30;
                    score.addPoints(TIME_LEFT_NAME, 30);
                    sd = true;
                }
            } else { //No one died..
                sendGameMessage("No team one that round :/");
                super.endGame();
            }
        }
        score.addPoints(TIME_LEFT_NAME, -1);
    }

    @Override
    public int getTimeout() {
        return 20;
    }
}