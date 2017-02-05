package net.battlenexus.paintball.entities.ai;

import java.lang.reflect.Field;

public class AIUtils {
    static Object getPrivateField(String fieldName, Class clazz, Object object) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }
}