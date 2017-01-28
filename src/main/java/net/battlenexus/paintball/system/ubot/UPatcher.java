package net.battlenexus.paintball.system.ubot;

import me.eddiep.ubot.UBot;
import me.eddiep.ubot.module.UpdateNotifier;
import me.eddiep.ubot.utils.Schedule;
import me.eddiep.ubot.utils.UpdateType;
import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.game.GameService;
import org.bukkit.Bukkit;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UPatcher implements UpdateNotifier {
    @Override
    public void onPreCheck(UBot uBot) {

    }

    @Override
    public Schedule<UpdateType> shouldBuild(UpdateType updateType, UBot uBot) {
        return Schedule.now(); //Always build
    }

    @Override
    public Schedule<UpdateType> shouldPatch(UpdateType updateType, UBot uBot) {
        if (updateType == UpdateType.BUGFIX) {
            Paintball.INSTANCE.getLogger().info("BugFix Patch detected! Will patch when server is empty");
            //Only patch bugs when the server is empty
            return Schedule.when(() -> Bukkit.getOnlinePlayers().size() == 0);
        } else if (updateType == UpdateType.MINOR) {
            Paintball.INSTANCE.getLogger().info("Minor Patch detected! Will patch when server is empty OR at midnight");
            //Patch minor updates when the server is empty or when it's midnight
            Calendar date = new GregorianCalendar();
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            date.add(Calendar.DAY_OF_MONTH, 1);

            final Date midnight = date.getTime();

            return Schedule.when(() -> {
                Date today = new Date();
                return Bukkit.getOnlinePlayers().size() == 0 || today.after(midnight);
            });

            /*return Schedule.combind(
                    Schedule.when(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return Bukkit.getOnlinePlayers().size() == 0;
                        }
                    }),
                    Schedule.at(date.getTime())
            );*/
        } else {
            Paintball.INSTANCE.getLogger().info(updateType.name() + " Patch detected! Will patch now");
            return Schedule.now();
        }
    }

    @Override
    public void patchComplete(UpdateType updateType, UBot uBot) {
        if (updateType == UpdateType.URGENT) {
            Paintball.INSTANCE.getLogger().info("Urgent patch applied. Will restart in 20 seconds");
            Paintball.sendGlobalWorldMessage("An urgent update needs to be patched!");
            Paintball.sendGlobalWorldMessage("The server will restart in 20 seconds.");
            GameService.getCurrentService().softStop();
            Paintball.INSTANCE.stopUbot();
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!GameService.getCurrentGame().hasEnded())
                GameService.getCurrentGame().endGame();
            return;
        }

        Paintball.INSTANCE.getLogger().info("Patch applied, restart has been queued for next game");
        GameService.getCurrentService().softStop();
        Paintball.INSTANCE.stopUbot();
    }

    @Override
    public void init() { }

    @Override
    public void deinit() { }
}
