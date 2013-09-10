package net.battlenexus.paintball.game;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.listeners.Tick;

public abstract class PaintballGame implements Tick {

    public void beginGame() {
        Paintball.INSTANCE.getTicker().addTick(this);
    }

    public abstract boolean isJoinable();

    public void addToGame(PBPlayer player) {

    }
}
