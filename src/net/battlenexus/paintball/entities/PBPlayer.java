package net.battlenexus.paintball.entities;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class PBPlayer {
    private static HashMap<String, PBPlayer> players = new HashMap<String, PBPlayer>();
    private Player player;

    private PBPlayer(Player player) {
        this.player = player;
    }

    /**
     * Creates a new {@link PBPlayer} for a player
     *
     * @param player Player the player you would like to find
     * @return PBPlayer
     */
    public static PBPlayer newPlayer(Player player) {
        PBPlayer pbPlayer = new PBPlayer(player);
        players.put(player.getName(), pbPlayer);

        return pbPlayer;
    }

    /**
     * Finds and returns the {@link PBPlayer} for the given player
     * @param player Player The player you would like to find
     * @return PBPlayer
     */
    public static PBPlayer getPlayer(Player player) {
        return PBPlayer.players.get(player.getName());
    }
}