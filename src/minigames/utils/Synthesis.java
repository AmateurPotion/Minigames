package minigames.utils;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Iconc;
import mindustry.type.Item;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Synthesis {
    public static final HashMap<String, Character> icons = new HashMap<>();
    private static final HashMap<Item, Character> itemIcons = new HashMap<>();

    public static void load() {
        Field[] names = Iconc.class.getFields();
        for(Field field : names) {
            if(field.getType() == char.class) {
                try {
                    icons.put(field.getName(), field.getChar(char.class));
                } catch (IllegalAccessException e) {
                    Log.info(e.getLocalizedMessage());
                }
            }
        }

        Field[] items = Items.class.getFields();
        for(Field field : items) {
            String t = (field.getName().charAt(0) + "").toUpperCase();
            String iconName = "item" + t + field.getName().substring(1);
            try {
                Item target = (Item) field.get(Item.class);
                itemIcons.put(target, icons.get(iconName));
            } catch (IllegalAccessException e) {
                Log.info(e.getLocalizedMessage());
            }
        }
    }

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

    public static char findIcon(String name) {
        return icons.getOrDefault(name, null);
    }

    public static char findItemIcon(@NotNull Item item) {
        return itemIcons.get(item);
    }

    public static Jval serialize(Jval.JsonMap origin) {
        Jval result = Jval.newObject();
        origin.forEach((val) -> {
            if(!val.key.contains("@NS")) {
                switch (val.value.getType()) {
                    case array -> result.add(val.key, serialize(val.value.asArray()));
                    case object -> result.add(val.key, serialize(val.value.asObject()));
                    default -> result.add(val.key, val.value);
                }
            }
        });

        return result;
    }

    public static Jval serialize(Jval.JsonArray origin) {
        Jval result = Jval.newArray();
        origin.forEach(val -> {
            switch (val.getType()) {
                case string -> {
                    if (!val.asString().contains("@NS")) {
                        result.add(val);
                    }
                }
                case array -> result.add(serialize(val.asArray()));
                case object -> result.add(serialize(val.asObject()));
                default -> result.add(val);
            }
        });
        return result;
    }
}
