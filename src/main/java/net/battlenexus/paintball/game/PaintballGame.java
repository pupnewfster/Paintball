package net.battlenexus.paintball.game;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.config.Config;
import net.battlenexus.paintball.listeners.Tick;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PaintballGame implements Tick {
    protected Config config;
    protected boolean ended = false;

    public void beginGame() {
        Paintball.INSTANCE.getTicker().addTick(this);
    }

    void setConfig(Config map_config) {
        config = map_config;
    }

    public void joinNextOpenTeam(PBPlayer p) {
        if (config.getBlueTeam().size() < config.getRedTeam().size()) {
            config.getBlueTeam().joinTeam(p);
        } else if (config.getRedTeam().size() < config.getBlueTeam().size()) {
            config.getRedTeam().joinTeam(p);
        } else {
            if (new Random().nextBoolean()) {
                config.getBlueTeam().joinTeam(p);
            } else {
                config.getRedTeam().joinTeam(p);
            }
        }
    }

    public void leaveGame(PBPlayer p) {
        Team team = getTeamForPlayer(p);
        if (team != null) {
            team.leaveTeam(p);
        }
    }

    public Team getTeamForPlayer(PBPlayer p) {
        if (config.getRedTeam().contains(p))
            return config.getRedTeam();
        else if (config.getBlueTeam().contains(p))
            return config.getBlueTeam();
        else
            return null;
    }

    public PBPlayer[] getAllPlayers() {
        List<PBPlayer> players = new ArrayList<PBPlayer>();
        players.addAll(config.getBlueTeam().getAllPlayers());
        players.addAll(config.getRedTeam().getAllPlayers());

        return players.toArray(new PBPlayer[players.size()]);
    }

    protected void endGame() {
        ended = true;
        _wakeup();
    }

    private synchronized void _wakeup() {
        super.notifyAll();
    }

    public synchronized void waitForEnd() throws InterruptedException {
        while (true) {
            if (ended)
                break;
            super.wait(0L);
        }
    }
}
