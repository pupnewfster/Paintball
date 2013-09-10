package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PBPlayer {
    private static HashMap<String, PBPlayer> players = new HashMap<String, PBPlayer>();
    private Player player;
    private int kills;
    private int deaths;
    private PaintballGame current_game;

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
     *
     * @param player Player The player you would like to find
     * @return PBPlayer
     */
    public static PBPlayer getPlayer(Player player) {
        return PBPlayer.players.get(player.getName());
    }

    public boolean isInGame() {
        return current_game != null;
    }

    public void kill() {
        if (!isInGame()) {
            return;
        }

        //TODO kill fireleaf
    }

    public void joinGame(PaintballGame game) {

    }
}