package net.battlenexus.paintball.system;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Set;
import java.util.UUID;

public class ScoreManager {
    private final ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;
    private Objective objective;

    public ScoreManager() {
        this.scoreboardManager = Bukkit.getScoreboardManager();
    }

    /**
     * Initialises the scoreboard
     *
     * @param name String Objective Name
     * @param key  String Objective Key
     */
    public void setupScoreboard(String name, String key) {
        scoreboard = scoreboardManager.getNewScoreboard();
        objective = scoreboard.registerNewObjective(key, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(name);
    }

    public void setDisplaySlot(DisplaySlot slot) {
        objective.setDisplaySlot(slot);
    }

    /**
     * Creates a new team with the given players
     *
     * @param teamName String The team name
     * @param players  OfflinePlayer[] Players you want to add to the team
     */
    public void addTeam(String teamName, OfflinePlayer[] players) {
        //Create the new team
        Team team = scoreboard.registerNewTeam(teamName);
        char[] teamChars = teamName.toCharArray();
        boolean hasColor = teamChars[0] == ChatColor.COLOR_CHAR;
        team.setPrefix(hasColor ? ("" + ChatColor.COLOR_CHAR + teamChars[1]) : "" + ChatColor.RESET);
        //Add players to that team
        for (OfflinePlayer player : players)
            team.addEntry(player.getUniqueId().toString());
        Score score = objective.getScore(players[0].getUniqueId().toString());
        score.setScore(0);
    }

    /**
     * Shows this scoreboard to everyone on the server
     */
    public void showScoreboard() {
        Set<String> players = scoreboard.getEntries();
        for (String uuid : players) {
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            if (p != null)
                p.setScoreboard(scoreboard);
        }
    }

    public void showScoreboardFor(Player p) {
        p.setScoreboard(scoreboard);
    }

    public void hideScoreboardFor(Player p) {
        p.setScoreboard(scoreboardManager.getNewScoreboard());
    }

    /**
     * Removes this scoreboard to everyone on the server
     */
    public void hideScoreboard() {
        Set<String> players = scoreboard.getEntries();
        for (String uuid : players) {
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            if (p != null)
                p.setScoreboard(scoreboardManager.getNewScoreboard());
        }
    }

    /**
     * Adds players to a team
     *
     * @param teamName String The name of the team
     * @param players  OfflinePlayer[] Players you want to add to the team
     */
    public void addPlayersToTeam(String teamName, OfflinePlayer[] players) {
        //Get the team
        Team team = scoreboard.getTeam(teamName);
        if (team == null)
            team = scoreboard.registerNewTeam(teamName);
        for (OfflinePlayer player : players)
            team.addEntry(player.getUniqueId().toString());
    }

    /**
     * Removes players from a team
     *
     * @param teamName String The name of the team
     * @param players  OfflinePlayer[] Players you want to remove from the team
     */
    public void removePlayersFromTeam(String teamName, OfflinePlayer[] players) {
        //Get the team
        Team team = scoreboard.getTeam(teamName);
        if (team == null)
            team = scoreboard.registerNewTeam(teamName);
        for (OfflinePlayer player : players)
            team.removeEntry(player.getUniqueId().toString());
    }

    /**
     * Adds points to a player's score
     *
     * @param player OfflinePlayer The Player you want to modify
     * @param amount int The points you want to add
     */
    public void addPoints(OfflinePlayer player, int amount) {
        Score score = objective.getScore(player.getUniqueId().toString());
        score.setScore(score.getScore() + amount);
    }

    /**
     * Takes points from a player's score
     *
     * @param player OfflinePlayer The player you want to modify
     * @param amount int The points you want to subtract
     */
    public void takePoints(OfflinePlayer player, int amount) {
        Score score = objective.getScore(player.getUniqueId().toString());
        score.setScore(score.getScore() - amount);
    }
}