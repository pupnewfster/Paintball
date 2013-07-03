package net.battlenexus.paintball;

import net.battlenexus.paintball.scoreboard.ScoreManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Paintball extends JavaPlugin {
    ScoreManager scoreboard;

    @Override
    public void onEnable() {
        scoreboard = new ScoreManager();
        //TODO Code
    }
}