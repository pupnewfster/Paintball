package net.battlenexus.paintball.commands.sign;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.listeners.Tick;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

public abstract class SignStat implements Tick {
    private Sign sign;
    private static ArrayList<SignStat> stats = new ArrayList<SignStat>();

    public static void addStat(SignStat s) {
        stats.add(s);
    }

    public static void saveSigns() {
        for (SignStat s : stats) {
            try {
                saveStat(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void diposeSigns() {
        for (SignStat s : stats) {
            Paintball.INSTANCE.getTicker().removeTick(s);
        }
    }

    public static void saveStat(SignStat s) throws IOException {
        String[] lines = new String[5];
        lines[0] = "" + s.sign.getLocation().getX();
        lines[1] = "" + s.sign.getLocation().getY();
        lines[2] = "" + s.sign.getLocation().getZ();
        lines[3] = "" + s.sign.getBlock().getWorld().getName();
        lines[4] = "" + s.getClass().getCanonicalName();

        File dir = Paintball.INSTANCE.getDataFolder();
        if (!dir.exists()) {
            boolean result = dir.mkdir();
        }
        Formatter formatter = new Formatter(new FileWriter(new File(dir, lines[0] + lines[1] + lines[2] + ".dat"), true));
        for (String line : lines) {
            formatter.out().append(line).append("\n");
        }
        formatter.close();
    }

    public static void loadStats() throws FileNotFoundException {
        File dir = Paintball.INSTANCE.getDataFolder();
        File[] maps = dir.listFiles();
        if (maps != null) {
            for (File f : maps) {
                if (f.isFile() && f.getName().endsWith(".dat")) {
                    double x = 0, y = 0, z = 0;
                    String w = null;
                    Class<?> class_ = null;
                    String NL = System.getProperty("line.separator");
                    Scanner scanner = new Scanner(new FileInputStream(f));
                    try {
                        int i = 0;
                        while (scanner.hasNextLine()){
                            switch (i) {
                                case 0:
                                    x = Double.parseDouble(scanner.nextLine());
                                    break;
                                case 1:
                                    y = Double.parseDouble(scanner.nextLine());
                                    break;
                                case 2:
                                    z = Double.parseDouble(scanner.nextLine());
                                    break;
                                case 3:
                                    w = scanner.nextLine();
                                    break;
                                case 4:
                                    try {
                                        class_ = Class.forName(scanner.nextLine());
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    scanner.nextLine();
                                    break;
                            }
                            i++;
                        }

                        if (w == null || class_ == null)
                            continue;

                        WorldCreator creator = new WorldCreator(w);
                        World world = Paintball.INSTANCE.getServer().createWorld(creator);
                        if (world == null)
                            continue;
                        Location l = new Location(world, x, y, z);
                        Block b = l.getBlock();
                        if (b.getType() != Material.SIGN && b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST)
                            continue;
                        Sign s = (Sign)b.getState();
                        SignStat sign = (SignStat) class_.getConstructor(Sign.class).newInstance(s);
                        stats.add(sign);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } finally{
                        scanner.close();
                    }
                }
            }
        }
    }

    public SignStat(Sign block) {
        Paintball.INSTANCE.getTicker().addTick(this);
        this.sign = block;
    }

    protected void updateSign(String... lines) {
        for (int i = 0; i < lines.length; i++) {
            sign.setLine(i, lines[i]);
        }
        sign.update(true);
    }

    protected Block getSignBlock() {
        return sign.getBlock();
    }

    protected Sign getSign() {
        return sign;
    }

    @Override
    public int getTimeout() {
        return 10;
    }
}
