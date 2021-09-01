package minigames.utils;

import java.lang.reflect.Field;

public class Synthesis {
    public static Object invoke(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    public static <T> T invokeT(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T)field.get(target);
    }

    public static <T> T invokeT(Class<T> type, Object target, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T)field.get(target);
    }
}
