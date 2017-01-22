package net.battlenexus.paintball.system;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

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
        scoreboard = scoreboardManager.getMainScoreboard();
        objective = scoreboard.getObjective(key);
        if (objective == null)
            objective = scoreboard.registerNewObjective(key, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(name);
    }

    public void setDisplaySlot(DisplaySlot slot) {
        objective.setDisplaySlot(slot);
    }

    /**
     * Creates a new team
     *
     * @param teamName String The team name
     */
    public void addTeam(String teamName) {
        if (teamName == null || teamName.equals(""))
            return;
        //Create the new team
        Team team = scoreboard.getTeam(teamName);
        if (team == null)
            scoreboard.registerNewTeam(teamName);
        if (team == null) //How can this be
            return;
        char[] teamChars = teamName.toCharArray();
        boolean hasColor = teamChars[0] == ChatColor.COLOR_CHAR;
        team.setPrefix(hasColor ? (ChatColor.COLOR_CHAR + "" + teamChars[1]) : "" + ChatColor.RESET);
        team.addEntry(teamName);
        Score score = objective.getScore(teamName);
        score.setScore(0);
    }

    /**
     * Remove a team
     *
     * @param teamName String The team name
     */
    public void removeTeam(String teamName) {
        //Remove the new team
        Team team = scoreboard.getTeam(teamName);
        if (team != null)
            team.unregister();
    }

    /**
     * Adds players to a team
     *
     * @param teamName String The name of the team
     * @param player   Player The player you want to add to the team
     */
    public void addPlayerToTeam(String teamName, Player player) {
        //Get the team
        Team team = scoreboard.getTeam(teamName);
        if (team == null)
            team = scoreboard.registerNewTeam(teamName);
        team.addEntry(player.getUniqueId().toString());
    }

    /**
     * Removes players from a team
     *
     * @param teamName String The name of the team
     * @param player   Player The player you want to remove from the team
     */
    public void removePlayerFromTeam(String teamName, Player player) {
        //Get the team
        Team team = scoreboard.getTeam(teamName);
        if (team == null)
            team = scoreboard.registerNewTeam(teamName);
        team.removeEntry(player.getUniqueId().toString());
    }

    /**
     * Adds points to a player's score
     *
     * @param value  OfflinePlayer The Player you want to modify
     * @param amount int The points you want to add
     */
    public void addPoints(String value, int amount) {
        Score score = objective.getScore(value);
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