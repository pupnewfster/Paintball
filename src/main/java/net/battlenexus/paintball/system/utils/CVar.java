package net.battlenexus.paintball.system.utils;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.game.config.ConfigItem;
import net.battlenexus.paintball.game.config.ReflectionConfig;
import net.battlenexus.paintball.game.config.impl.StringHashMapConfig;

import java.io.File;
import java.io.IOException;

public class CVar extends ReflectionConfig {
    public static final CVar INSTANCE = new CVar();
    @ConfigItem
    private StringHashMapConfig hashMapConfig = new StringHashMapConfig();

    public static void loadVars() {
        INSTANCE.loadvars();
    }

    public void loadvars() {
        File f = new File(Paintball.INSTANCE.getDataFolder(), "cvars.config");
        if (!f.exists()) {
            try {
                boolean created = f.createNewFile();
                if (!created)
                    return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            super.parseFile(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isRegistered(String key) {
        return INSTANCE.isregistered(key);
    }

    public boolean isregistered(String key) {
        return hashMapConfig.containsKey(key);
    }

    public static String getVar(String key) {
        return INSTANCE.getvar(key);
    }

    public String getvar(String key) {
        return hashMapConfig.get(key);
    }

    public static void registerVar(String key, String value) {
        INSTANCE.registervar(key, value);
    }

    public void registervar(String key, String value) {
        hashMapConfig.put(key, value);
        try {
            saveToFile("cvars.config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getdouble(String key) {
        try {
            return Double.parseDouble(getvar(key));
        } catch (Throwable t) {
            return 0;
        }
    }

    public float getfloat(String key) {
        try {
            return Float.parseFloat(getvar(key));
        } catch (Throwable t) {
            return 0f;
        }
    }

    public byte getbyte(String key) {
        try {
            return Byte.parseByte(getvar(key));
        } catch (Throwable t) {
            return 0;
        }
    }

    public boolean getboolean(String key) {
        try {
            return getvar(key).toLowerCase().contains("t") || getvar(key).toLowerCase().contains("y");
        } catch (Throwable t) {
            return false;
        }
    }

    public long getlong(String key) {
        try {
            return Long.parseLong(getvar(key));
        } catch (Throwable t) {
            return 0;
        }
    }

    public int getint(String key) {
        try {
            return Integer.parseInt(getvar(key));
        } catch (Throwable t) {
            return 0;
        }
    }

    public static int getInt(String key) {
        return INSTANCE.getint(key);
    }

    public static double getDouble(String key) {
        return INSTANCE.getdouble(key);
    }

    public static boolean getBoolean(String key) {
        return INSTANCE.getboolean(key);
    }

    public static float getFloat(String key) {
        return INSTANCE.getfloat(key);
    }

    public static long getLong(String key) {
        return INSTANCE.getlong(key);
    }

    public static byte getByte(String key) {
        return INSTANCE.getbyte(key);
    }
}