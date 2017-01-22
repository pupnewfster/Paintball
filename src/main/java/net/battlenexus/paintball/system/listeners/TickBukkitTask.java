package net.battlenexus.paintball.system.listeners;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class TickBukkitTask extends BukkitRunnable {
    private final ArrayList<TickData> ticks = new ArrayList<>();

    @Override
    public void run() {
        synchronized (ticks) {
            TickData[] tick_array = ticks.toArray(new TickData[ticks.size()]);
            for (TickData t : tick_array) {
                if (t.tick == null) {
                    ticks.remove(t);
                    continue;
                }
                if (t.tick.getTimeout() == 0)
                    continue;
                t.time++;
                if (t.time >= t.tick.getTimeout()) {
                    t.time = 0;
                    t.tick.tick();
                }
            }
        }
    }

    public void addTick(Tick tick) {
        TickData data = new TickData();
        data.tick = tick;
        ticks.add(data);
    }

    public void removeTick(Tick tick) {
        synchronized (ticks) {
            for (int i = 0; i < ticks.size(); i++)
                if (ticks.get(i).tick.equals(tick)) {
                    ticks.remove(i);
                    break;
                }
        }
    }

    private class TickData {
        public Tick tick;
        public int time;
    }
}