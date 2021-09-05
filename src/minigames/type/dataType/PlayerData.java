package minigames.type.dataType;

import arc.struct.Seq;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import minigames.type.ctype.CoolTime;
import minigames.type.ctype.Skill;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import static minigames.Entry.*;

public class PlayerData {
    public final Player player;
    public final Seq<Skill<?>> skillSet;
    public final Jval config;
    public final CoolTime coolTimes;
    private final HashMap<String, Property<?>> props;

    public PlayerData(Player player) {
        this(player, db.skills);
    }

    public PlayerData(Player player, Seq<Skill<?>> originalSkills) {
        // init
        this.player = player;
        skillSet = Seq.with();
        config = Objects.requireNonNullElseGet(db.getPlayerData(player.uuid()), Jval::newObject);
        coolTimes = new CoolTime();
        props = new HashMap<>();

        // configuration
        if(config.get("skillSet") != null) {
            originalSkills.each(skill -> config.get("skillSet").asArray().contains(val -> Objects.equals(val.toString(), skill.name())), skillSet::add);
        }
        config.remove("skillSet");

        configCheck("name", player.name());
        configCheck("permission level", 0);
        configCheck("score", 0);
        configCheck("refineChance", 0);
        configCheck("line@NS", 1);

        if(config.get("items") != null && config.get("items").getType() == Jval.Jtype.object) {
            Jval items = config.get("items");
            items.asObject().forEach((e) -> {
                if(e.value.getType() == Jval.Jtype.number) {
                    Item target = Vars.content.items().find(item -> Objects.equals(item.name, e.key));
                    int amount = e.value.asInt();
                    Property<Integer> val = new Property<>(amount, Jval.valueOf(amount));
                    props.put("item-" + target.name, val);
                }
            });
        } else {
            Vars.content.items().each(item -> {
                Property<Integer> val = new Property<>(0, Jval.valueOf(0));
                props.put("item-" + item.name, val);
            });
        }
        config.remove("items");
    }

    private void configCheck(String name, Object val) {
        if(config.get(name) == null) {
            if(val instanceof Jval obj) config.put(name, obj);
            else if(val instanceof String obj) config.put(name, obj);
            else if(val instanceof Number obj) config.put(name, obj);
            else if(val instanceof Boolean obj) config.put(name, obj);
        }
    }

    public int addConfigInt(String name, int count) {
        int result = config.getInt(name, 0) + count;
        config.put(name, result);
        return result;
    }

    public int permissionLevel() {
        return config.getInt("permission level", 0);
    }

    public void permissionLevel(int level) {
        config.put("permission level", level);
    }

    public boolean isReady(String name) {
        return coolTimes.get(name) == 0;
    }

    public <T> HashMap<String, Property<T>> props(Class<T> type) {
        //     public final HashMap<String, Property<?>> props;
        HashMap<String, Property<T>> result = new HashMap<>();
        props.forEach((key, prop) -> {
            if(prop.val.getClass() == type) {
                result.put(key, (Property<T>) prop);
            }
        });
        return result;
    }

    public int item(@NotNull Item type) {
        if(props.containsKey("item-" + type.name) && props.get("item-" + type.name).val instanceof Integer amount) {
            return amount;
        } else {
            return 0;
        }
    }

    public void item(@NotNull Item type, int amount) {
        props.put("item-" + type.name, new Property<>(amount, Jval.valueOf(amount)));
    }

    public int addItem(@NotNull Item type, int amount) {
        int result = item(type) + amount;
        props.put("item-" + type.name, new Property<>(amount, Jval.valueOf(amount)));
        return result;
    }

    public HashMap<Item, Property<Integer>> items() {
        HashMap<Item, Property<Integer>> result = new HashMap<>();

        props(Integer.class).forEach((name, val) -> {
            if(name.contains("item-")) {
                String itemName = name.replace("item-", "");
                Item target = Vars.content.items().find(item -> Objects.equals(item.name, itemName));
                if(target != null) {
                    result.put(target, val);
                }
            }
        });

        return result;
    }
}