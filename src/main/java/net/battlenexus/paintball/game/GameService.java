package net.battlenexus.paintball.game;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.config.Config;
import net.battlenexus.paintball.game.impl.SimpleGame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GameService {
    private static final Class<?>[] GAME_TYPES = new Class[] {
            SimpleGame.class
    };

    private ArrayList<Config> configs = new ArrayList<Config>();
    private PaintballGame game;
    private ArrayList<PBPlayer> joinnext = new ArrayList<PBPlayer>();
    private Config nextconfig;
    private boolean running = true;
    private boolean waiting = false;
    private Thread current_thread;

    public void loadMaps() {
        File dir = Paintball.INSTANCE.getDataFolder();
        File[] maps = dir.listFiles();
        if (maps != null) {
            for (File f : maps) {
                if (f.isFile() && f.getName().endsWith(".xml")) {
                    Config c = new Config();
                    try {
                        c.parseFile(f);
                        configs.add(c);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void play() {
        if (configs.size() == 0) {
            Paintball.INSTANCE.error("No maps to play on!");
            return;
        }
        if (GAME_TYPES.length == 0) {
            Paintball.INSTANCE.error("No gamemodes to play with!");
            return;
        }

        current_thread = Thread.currentThread();
        final Random random = new Random();
        while (running) {
            try {
                int map_id = random.nextInt(configs.size());
                Config map_config = new Config(configs.get(map_id)); //Make a clone of the config
                this.nextconfig = map_config;
                int game_id = random.nextInt(GAME_TYPES.length);
                game = createGame((Class<? extends PaintballGame>) GAME_TYPES[game_id]); //Weak typing because fuck it
                game.setConfig(map_config);

                Paintball.sendGlobalWorldMessage("The next map will be " + map_config.getMapName() + "!");

                Paintball.sendGlobalWorldMessage("The game will start in 120 seconds.");
                try {
                    Thread.sleep(100000);
                    Paintball.sendGlobalWorldMessage(ChatColor.RED + "Game will start in 20 seconds!");
                    Thread.sleep(10000);
                    Paintball.sendGlobalWorldMessage(ChatColor.DARK_RED + "10 seconds!");
                    Thread.sleep(5000);
                    for (int i = 0; i < 5; i++) {
                        Paintball.sendGlobalWorldMessage("" + ChatColor.DARK_RED + (5 - i) + "!");
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PBPlayer[] bukkit_players = joinnext.toArray(new PBPlayer[joinnext.size()]);
                for (PBPlayer p : bukkit_players) {
                    if (Paintball.INSTANCE.isPlayingPaintball(p)) {
                        joinnext.remove(p);
                        p.joinGame(game);
                    }
                }
                game.beginGame();
                try {
                    waiting = true;
                    game.waitForEnd();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (!game.ended) {
                        game.endGame();
                    }
                }
                waiting = false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (!running)
            return;
        running = false;
        current_thread.interrupt();
        try {
            current_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        current_thread = null;
        joinnext.clear();
    }

    public PaintballGame getCurrentGame() {
        return game;
    }

    public boolean isGameInProgress() {
        return waiting;
    }

    public boolean canJoin() {
        return nextconfig.getPlayerMax() < joinnext.size();
    }

    public int getMaxPlayers() {
        return nextconfig.getPlayerMax();
    }

    public String getMapName() {
        return nextconfig.getMapName();
    }

    public int getQueueCount() {
        return joinnext.size();
    }

    public boolean joinNextGame(Player p) {
        PBPlayer pb = PBPlayer.toPBPlayer(p);
        return joinNextGame(pb);
    }

    public boolean joinNextGame(PBPlayer pb) {
        if (joinnext.contains(pb))
            return false;
        joinnext.add(pb);
        return true;
    }

    public boolean leaveQueue(PBPlayer pb) {
        if (!joinnext.contains(pb))
            return false;
        joinnext.remove(pb);
        return true;
    }

    public PaintballGame createGame(Class<? extends PaintballGame> class_) throws IllegalAccessException, InstantiationException {
        return class_.newInstance();
    }
}
