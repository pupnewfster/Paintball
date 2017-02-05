package net.battlenexus.paintball.entities.ai;

import net.battlenexus.paintball.entities.BasePlayer;

public interface AIPlayer extends BasePlayer { //TODO: Move some logic into here when there is more than one AI player type
    void remove();
}