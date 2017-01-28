package net.battlenexus.paintball.system.ubot;

import me.eddiep.ubot.module.Logger;
import net.battlenexus.paintball.Paintball;

public class ULogger implements Logger {
    @Override
    public void log(String s) {
        Paintball.INSTANCE.getLogger().info("UBOT - " + s);
    }

    @Override
    public void warning(String s) {
        Paintball.INSTANCE.getLogger().warning("UBOT - " + s);
    }

    @Override
    public void init() {
    }

    @Override
    public void deinit() {
    }
}
