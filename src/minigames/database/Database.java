package minigames.database;

import arc.Events;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import minigames.io.CommandLoader;
import minigames.modes.GameMode;
import minigames.modes.duel.Duel;
import minigames.modes.marathon.Marathon;
import minigames.modes.marathon.MarathonEvents;
import minigames.modes.solo.Solo;
import minigames.type.dataType.PlayerData;
import minigames.type.ctype.Skill;
import minigames.io.EventList;
import minigames.utils.MenuManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static arc.util.Log.err;

public class Database {
    public final Fi databaseDirectory;
    public final Seq<PlayerData> players;
    public final Seq<Skill<?>> skills;
    public final MenuManager menu;
    public final Bundle bundle;
    public final HashMap<String, GameMode> gameMode;
    public final CommandLoader cLoader = new CommandLoader();
    private final HashMap<String, Jval> channels;
    private final NullGameMode nullMode = new NullGameMode();

    public Database() {
        // init
        databaseDirectory = new Fi(Vars.dataDirectory.path() + "/database", Vars.dataDirectory.type());
        databaseDirectory.mkdirs();
        players = Seq.with();
        skills = Seq.with();
        gameMode = new HashMap<>();
        menu = new MenuManager();
        bundle = new Bundle();
        channels = new HashMap<>();

        // game mode setup
        GameMode marathon = new Marathon();
        GameMode duel = new Duel();
        GameMode solo = new Solo();
        gameMode.put(marathon.name(), marathon);
        gameMode.put(duel.name(), duel);
        gameMode.put(solo.name(), solo);
    }

    public void loadSkillEvent() {
        Events.on(EventType.PlayerChatEvent.class, e -> runSkill(EventType.PlayerChatEvent.class, e, e.player.team(), e.player.unit() != null ? e.player.unit().type() : null, e.player));
        Events.on(EventType.WithdrawEvent.class, e -> runSkill(EventType.WithdrawEvent.class, e, e.player.team(), e.player.unit() != null ? e.player.unit().type() : null, e.player));
        Events.on(EventType.DepositEvent.class, e -> runSkill(EventType.DepositEvent.class, e, e.player.team(), e.player.unit() != null ? e.player.unit().type() : null, e.player));
        Events.on(EventType.ConfigEvent.class, e -> runSkill(EventType.ConfigEvent.class, e, e.player.team(), e.player.unit() != null ? e.player.unit().type() : null, e.player));
        Events.on(EventType.TapEvent.class, e -> runSkill(EventType.TapEvent.class, e, e.player.team(), e.player.unit() != null ? e.player.unit().type() : null, e.player));
        Events.on(EventType.PickupEvent.class, e -> runSkill(EventType.PickupEvent.class, e, e.carrier.team(), e.carrier.type(), getPlayer(e.carrier.team())));
        Events.on(EventType.UnitControlEvent.class, e -> runSkill(EventType.UnitControlEvent.class, e, e.player.team(), e.unit != null ? e.unit.type() : null, e.player));
        Events.on(EventType.GameOverEvent.class, e -> runSkill(EventType.GameOverEvent.class, e, e.winner, getPlayerUnitType(e.winner), getPlayer(e.winner)));
        Events.on(EventType.BuildTeamChangeEvent.class, e -> runSkill(EventType.BuildTeamChangeEvent.class, e, e.build.team(), getPlayerUnitType(e.build.team()), getPlayer(e.build.team())));
        Events.on(EventType.CoreChangeEvent.class, e -> runSkill(EventType.CoreChangeEvent.class, e, e.core.team(), getPlayerUnitType(e.core.team()), getPlayer(e.core.team())));
        Events.on(EventType.BlockBuildBeginEvent.class, e -> runSkill(EventType.BlockBuildBeginEvent.class, e, e.team, e.unit != null ? e.unit.type() : null, getPlayer(e.team)));
        Events.on(EventType.BlockBuildEndEvent.class, e -> runSkill(EventType.BlockBuildEndEvent.class, e, e.team, e.unit != null ? e.unit.type() : null, getPlayer(e.team)));
        Events.on(EventType.BuildSelectEvent.class, e -> runSkill(EventType.BuildSelectEvent.class, e, e.team, e.builder.type(), getPlayer(e.team)));
        Events.on(EventType.UnitDestroyEvent.class, e -> runSkill(EventType.UnitDestroyEvent.class, e, e.unit.team(), e.unit.type(), getPlayer(e.unit.team())));
        Events.on(EventType.UnitDrownEvent.class, e -> runSkill(EventType.UnitDrownEvent.class, e, e.unit.team(), e.unit.type(), getPlayer(e.unit.team())));
        Events.on(EventType.UnitCreateEvent.class, e -> runSkill(EventType.UnitCreateEvent.class, e, e.unit.team(), e.unit.type(), getPlayer(e.unit.team())));
        Events.on(EventType.UnitUnloadEvent.class, e -> runSkill(EventType.UnitUnloadEvent.class, e, e.unit.team(), e.unit.type(), getPlayer(e.unit.team())));
        Events.on(EventType.UnitChangeEvent.class, e -> runSkill(EventType.UnitChangeEvent.class, e, e.unit.team(), e.unit.type(), e.player));
        Events.on(MarathonEvents.MarathonLineArrivalEvent.class, e -> runSkill(MarathonEvents.MarathonLineArrivalEvent.class, e, e.player().team(), e.unit().type(), e.player()));
        Events.on(EventList.BindingReceiveEvent.class, e -> runSkill(EventList.BindingReceiveEvent.class, e, e.player().team(), e.player().unit() != null ? e.player().unit().type() : null, e.player()));
    }

    private <T> void runSkill(Class<T> event, T arg, Team team, UnitType type, Player player) {
        skills.<Skill<T>>each(skill -> (skill.team() == null || skill.team() == team) && (skill.unitType() == null || skill.unitType() == type) && skill.entry() == event,
                skill -> skill.listener().get(arg));
        players.each(data -> data.player == player,
                playerData -> playerData.skillSet.<Skill<T>>each(skill -> skill.entry() == event,
                        skill -> skill.listener().get(arg)));
    }

    public <T> Skill<T> skill(String name) {
        if(skills.contains(skill -> Objects.equals(skill.name(), name))) {
            return (Skill<T>) skills.find(skill -> skill.name() == name);
        } else {
            return null;
        }
    }

    public Seq<String> currentSkills(PlayerData data) {
        Seq<String> tags = Seq.with("@NS", "@DR", "#");
        Seq<String> skillList = Seq.with();
        Team tc = data.player.team();
        UnitType utc = getPlayerUnitType(data.player.team());

        data.skillSet.each(skill -> {
            String[] skillName = new String[]{skill.name()};
            tags.each(tag -> {
                skillName[0] = skillName[0].replaceAll(tag, "");
            });
            if(!skillList.contains(skillName[0]) && !skillName[0].contains("@ND")) {
                skillList.add(skillName[0]);
            }
        });
        skills.each(skill -> (utc != null && skill.unitType() == utc) || skill.team() == tc, skill -> {
            String[] skillName = new String[]{skill.name()};
            tags.each(tag -> {
                skillName[0] = skillName[0].replaceAll(tag, "");
            });
            if(!skillList.contains(skillName[0])) {
                skillList.add(skillName[0]);
            }
        });

        return skillList;
    }

    public String skillDescription(String skillName, Locale locale) {
        ResourceBundle b = bundle.bundle(locale);
        if (b == null) {
            b = bundle.bundle(Locale.getDefault());
        }
        return b.getString("skill." + skillName + ".description");
    }

    public void savePlayerData(PlayerData data) {
        Fi path = new Fi(databaseDirectory.path() + "/" + data.player.uuid().replaceAll("/", "_") + ".json");
        Jval save = Jval.newObject();
        Jval skillSet = Jval.newArray();

        // default values
        data.config.asObject().forEach(e -> {
            if(!e.key.contains("@NS")) {
                save.put(e.key, e.value);
            }
        });

        // skills
        data.skillSet.each(skill -> skillSet.add(skill.name()));
        save.put("skillSet", skillSet);

        // items
        Jval items = Jval.newObject();
        data.items().forEach((item, amount) -> items.put(item.name, amount.val));
        save.put("items", items);

        path.writeString(save.toString(Jval.Jformat.formatted), false);
    }

    public @Nullable Jval getPlayerData(String uuid) {
        Jval data;
        Fi path = new Fi(databaseDirectory.path() + "/" + uuid.replaceAll("/", "_") + ".json");

        if(path.exists()) {
            data = Jval.read(path.reader());
            return data;
        } else {
            return null;
        }
    }

    public Seq<Jval> getAllPlayerData() {
        Seq<Jval> result = Seq.with();

        databaseDirectory.findAll().each(path -> {
            String uuid = path.name().replaceAll("\\.json", "");
            result.add(getPlayerData(uuid));
        });

        return result;
    }

    public GameMode gameMode(String modeName) {
        GameMode result = gameMode.get(modeName);
        if(result != null) {
            return result;
        } else {
            err("can't find mode : " + modeName);
            return nullMode;
        }
    }

    public <T extends GameMode> T gameMode(Class<T> type, String modeName) {
        GameMode result = gameMode.get(modeName);
        if(result != null && result.getClass() == type) {
            return (T) result;
        } else {
            err("can't find mode : " + modeName);
            return null;
        }
    }

    public PlayerData find(String playerName) {
        return players.find(data -> Objects.equals(data.player.name(), playerName));
    }

    public PlayerData find(Player player) {
        return players.find(data -> data.player == player);
    }

    public Player getPlayer(Team team) {
        if(gameMode("solo").isActive() && team != null) {
            return Groups.player.find(player -> player.team() == team);
        } else {
            return null;
        }
    }

    public UnitType getPlayerUnitType(Team team) {
        if(gameMode("solo").isActive()) {
            Player p = Groups.player.find(player -> player.team() == team);
            if(p != null) {
                Unit u = p.unit();
                if(u != null) {
                    return u.type();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public <T> boolean registerSkill(Skill<T> skill) {
        if(skills.contains(s -> Objects.equals(s.name(), skill.name()))) {
            return false;
        } else {
            skills.add(skill);
            return true;
        }
    }

    public Seq<Skill<?>> teamSkills(Team team) {
        return skills.copy().filter(skill -> skill.team() == team && skill.unitType() == null);
    }

    public Seq<Skill<?>> unitSkills(UnitType type) {
        return skills.copy().filter(skill -> skill.team() == null && skill.unitType() == type);
    }

    public void loadDBChannel(Fi path) {
        if(path.exists()) {
            if(path.isDirectory()) {
                path.findAll().each(fi -> {
                    if(!fi.isDirectory() && fi.name().contains(".json")) {
                        Jval channel = Jval.read(fi.reader());
                        String channelName = fi.file().getName().replaceAll("\\.json", "");
                        if(channel.isObject() && !channels.containsKey(channelName)) {
                            channels.put(channelName, channel);
                            Log.info(channelName);
                        }
                    } else if(fi.isDirectory()) {
                        loadDBChannel(fi);
                    }
                });
            } else {
                try {
                    Jval channel = Jval.read(path.reader());
                    String channelName = path.file().getName().replaceAll("\\.json", "");
                    if(channel.isObject() && !channels.containsKey(channelName)) {
                        channels.put(channelName, channel);
                    }
                } catch (ArcRuntimeException e) {
                    Log.info(e.getLocalizedMessage());
                }
            }
        }
    }

    private static class NullGameMode implements GameMode {
        private final Jval config = Jval.newObject();

        @Override
        public boolean active() {
            return false;
        }

        @Override
        public void disable() {

        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public String name() {
            return "null";
        }

        @Override
        public @NotNull Jval config() {
            return config;
        }
    }
}
