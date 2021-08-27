package minigames.database;

import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.type.UnitType;
import mindustry.ui.Menus;
import minigames.database.DataType.PlayerData;
import minigames.ctype.Skill;
import minigames.events.EventList;
import minigames.function.MenuManager;
import minigames.modes.marathon.skills.UnitSkills;

import java.util.HashMap;
import java.util.Objects;

import static arc.util.Log.err;

public class Database {
    public final UnitSkills skillLoader;
    public final Fi databaseDirectory;
    public final Seq<PlayerData> players;
    public final Seq<Skill<?>> skills;
    public final MenuManager menu;
    public final Bundle bundle;
    private final HashMap<String, Boolean> gameMode;

    public Database() {
        // init
        databaseDirectory = new Fi(Vars.dataDirectory.path() + "/database", Vars.dataDirectory.type());
        databaseDirectory.mkdirs();
        players = Seq.with();
        skills = Seq.with();
        gameMode = new HashMap<>();
        skillLoader = new UnitSkills();
        menu = new MenuManager();
        bundle = new Bundle();

        // game mode setup
        gameMode.put("shuffle", false);
        gameMode.put("solo", false);
        gameMode.put("marathon", false);
        gameMode.put("duel", false);
        gameMode.put("starlike", false);
        gameMode.put("story", false);
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
        Events.on(EventList.MarathonLineArrivalEvent.class, e -> runSkill(EventList.MarathonLineArrivalEvent.class, e, e.player().team(), e.unit().type(), e.player()));
    }

    private <T> void runSkill(Class<T> event, T arg, Team team, UnitType type, Player player) {
        skills.<Skill<T>>each(skill -> (skill.team() == null || skill.team() == team) && (skill.unitType() == null || skill.unitType() == type) && skill.entry() == event, skill -> skill.listener().get(arg));
        players.each(data -> data.player == player, playerData -> playerData.skillSet.<Skill<T>>each(skill -> skill.entry() == event, skill -> skill.listener().get(arg)));
    }

    public <T> Skill<T> skill(String name) {
        if(skills.contains(skill -> Objects.equals(skill.name(), name))) {
            return (Skill<T>) skills.find(skill -> skill.name() == name);
        } else {
            return null;
        }
    }

    public void savePlayerData(PlayerData data) {
        Fi path = new Fi(databaseDirectory.path() + "/" + data.player.uuid().replaceAll("/", "_") + ".json");
            Jval save = Jval.newObject();
            Jval skillSet = Jval.newArray();

            data.config.asObject().forEach(e -> {
                if(!e.key.contains("@NS")) {
                    save.put(e.key, e.value);
                }
            });
            data.skillSet.each(skill -> skillSet.add(skill.name()));
            save.put("skillSet", skillSet);

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

    public boolean gameMode(String modeName) {
        if(gameMode.get(modeName) != null) {
            return gameMode.get(modeName);
        } else {
            err("can't find mode : " + modeName);
            return false;
        }
    }

    public void gameMode(String modeName, boolean active) {
        gameMode.put(modeName, active);
    }

    public PlayerData find(String playerName) {
        return players.find(data -> Objects.equals(data.player.name(), playerName));
    }

    public PlayerData find(Player player) {
        return players.find(data -> data.player == player);
    }

    public Player getPlayer(Team team) {
        if(gameMode("solo") && team != null) {
            return Groups.player.find(player -> player.team() == team);
        } else {
            return null;
        }
    }

    public UnitType getPlayerUnitType(Team team) {
        if(gameMode("solo")) {
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
}
