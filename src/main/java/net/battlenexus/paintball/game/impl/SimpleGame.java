package net.battlenexus.paintball.game.impl;

import net.battlenexus.paintball.game.PaintballGame;

public class SimpleGame extends PaintballGame {
    @Override
    public boolean isJoinable() {
        return false;
    }

    @Override
    public void tick() {

    }

    @Override
    public int getTimeout() {
        return 0;
    }
}
