package net.battlenexus.paintball;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.listeners.PlayerListener;
import net.battlenexus.paintball.listeners.TickBukkitTask;
import net.battlenexus.paintball.scoreboard.ScoreManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Paintball extends JavaPlugin {
    public static Paintball INSTANCE;
    private TickBukkitTask tasks;
    ScoreManager scoreboard;

    @Override
    public void onEnable() {
        INSTANCE = this;
        scoreboard = new ScoreManager();

        //Register Listeners
        registerListeners();

        tasks = new TickBukkitTask();
        tasks.runTaskTimerAsynchronously(this, 1, 1);

        for(Player player : getServer().getOnlinePlayers()) {
            PBPlayer.newPlayer(player);
        }
    }

    private void registerListeners() {
        new PlayerListener(this);
    }

    public TickBukkitTask getTicker() {
        return tasks;
    }

    public void error(String message) {
        getLogger().info("[BN Paintball] ERROR: " + message);
    }
}