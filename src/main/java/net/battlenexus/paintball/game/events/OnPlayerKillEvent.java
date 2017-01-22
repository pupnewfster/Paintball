package net.battlenexus.paintball.game.events;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.PaintballGame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OnPlayerKillEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private PBPlayer victim;
    private PBPlayer thrower;
    private PaintballGame game;

    public OnPlayerKillEvent(PBPlayer hit, PBPlayer thrower, PaintballGame game) {
        this.game = game;
        this.victim = hit;
        this.thrower = thrower;
    }

    public PBPlayer getPBPlayer() {
        return victim;
    }

    public Player getPlayer() {
        return victim.getBukkitPlayer();
    }

    public PBPlayer getPBThrower() {
        return thrower;
    }

    public Player getThrower() {
        return thrower.getBukkitPlayer();
    }

    public PaintballGame getGame() {
        return game;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}