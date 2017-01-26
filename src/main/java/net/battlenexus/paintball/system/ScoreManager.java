package net.battlenexus.paintball.system;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public class ScoreManager {
    private Scoreboard scoreboard;
    private Objective objective;

    /**
     * Initialises the scoreboard
     *
     * @param name String Objective Name
     * @param key  String Objective Key
     */
    public void setupScoreboard(String name, String key) {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        objective = scoreboard.getObjective(key);
        if (objective == null)
            objective = scoreboard.registerNewObjective(key, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(name);
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
            team = scoreboard.registerNewTeam(teamName);
        boolean hasColor = teamName.startsWith(ChatColor.COLOR_CHAR + "") && teamName.length() > 2;
        team.setPrefix(hasColor ? (ChatColor.COLOR_CHAR + "" + teamName.charAt(1)) : "" + ChatColor.RESET);
        team.addEntry(teamName);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
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
     * Adds a player to a team
     *
     * @param teamName String The name of the team
     * @param player   Player The player you want to add to the team
     */
    public void addPlayerToTeam(String teamName, Player player) {
        addUUIDToTeam(teamName, player.getUniqueId());
    }

    /**
     * Adds a uuid to a team
     *
     * @param teamName String The name of the team
     * @param uuid     UUID The uuid you want to add to the team
     */
    public void addUUIDToTeam(String teamName, UUID uuid) {
        //Get the team
        Team team = scoreboard.getTeam(teamName);
        if (team == null)
            team = scoreboard.registerNewTeam(teamName);
        team.addEntry(uuid.toString());
    }

    /**
     * Removes a player from a team
     *
     * @param teamName String The name of the team
     * @param player   Player The player you want to remove from the team
     */
    public void removePlayerFromTeam(String teamName, Player player) {
        removeUUIDFromTeam(teamName, player.getUniqueId());
    }

    /**
     * Removes a uuid from a team
     *
     * @param teamName String The name of the team
     * @param uuid     UUID The uuid you want to remove from the team
     */
    public void removeUUIDFromTeam(String teamName, UUID uuid) {
        //Get the team
        Team team = scoreboard.getTeam(teamName);
        if (team == null)
            team = scoreboard.registerNewTeam(teamName);
        team.removeEntry(uuid.toString());
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