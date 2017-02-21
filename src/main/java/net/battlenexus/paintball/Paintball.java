package net.battlenexus.paintball;

import me.eddiep.ubot.UBot;
import me.eddiep.ubot.utils.CancelToken;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.ai.CustomEntities;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.system.commands.PBCommandHandler;
import net.battlenexus.paintball.system.commands.sign.SignStat;
import net.battlenexus.paintball.system.listeners.PlayerListener;
import net.battlenexus.paintball.system.listeners.TickBukkitTask;
import net.battlenexus.paintball.system.ubot.ULogger;
import net.battlenexus.paintball.system.ubot.UPatcher;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;

public class Paintball extends JavaPlugin {
    public static Paintball INSTANCE;
    public World paintball_world;
    private TickBukkitTask tasks;
    private BukkitRunnable gameTask;
    private CancelToken ubotCancelToken;
    private GameService game;

    public GameService getGameService() {
        return game;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        CustomEntities.registerEntities();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        PBCommandHandler handler = new PBCommandHandler();
        getCommand("createpbmap").setExecutor(handler);
        getCommand("cpbmap").setExecutor(handler);
        getCommand("join").setExecutor(handler);
        getCommand("leave").setExecutor(handler);
        getCommand("signstat").setExecutor(handler);
        getCommand("spectate").setExecutor(handler);
        getCommand("createbots").setExecutor(handler);

        //Register Listeners
        registerListeners();

        getLogger().info("Starting UBot");
        try {
            UBot ubot = new UBot(new File("/home/minecraft/ubot/Paintball"), new UPatcher(), new ULogger());
            ubotCancelToken = ubot.startAsync();
        } catch (Exception e) {
            getLogger().warning("Failed to start UBot");
        }

        tasks = new TickBukkitTask();
        tasks.runTaskTimer(this, 1, 1);

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                game = new GameService();
                game.loadMaps();
                game.play();
            }
        };
        gameTask.runTaskAsynchronously(this);

        try {
            SignStat.loadStats();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        CustomEntities.unregisterEntities();
        SignStat.saveSigns();
        SignStat.disposeSigns();
        //TODO cancel the gameTask potentially?
        if (game != null)
            game.stop();
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

    private void sendWorldMessage(String message) {
        for (Player p : getServer().getOnlinePlayers())
            if (p.getLocation().getWorld().getName().equals(paintball_world.getName()))
                p.sendMessage(message);
    }

    private boolean isPlayingPaintball(Player player) {
        return player.getLocation().getWorld().getName().equals(paintball_world.getName());
    }

    public boolean isPlayingPaintball(PBPlayer player) {
        return isPlayingPaintball(player.getBukkitPlayer());
    }

    public static void sendGlobalWorldMessage(String message) {
        INSTANCE.sendWorldMessage(formatMessage(message));
    }

    public static String formatMessage(String message) {
        return ChatColor.WHITE + "[" + ChatColor.RED + "Paintball" + ChatColor.WHITE + "] " + ChatColor.GRAY + message;
    }

    public void stopUbot() {

    }

    public void changeServer(Player p) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bs);
        dos.writeUTF("Connect");
        dos.writeUTF("lobby");
        p.sendPluginMessage(this, "BungeeCord", bs.toByteArray());
        bs.close();
        dos.close();
    }
}