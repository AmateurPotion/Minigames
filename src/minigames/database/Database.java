package minigames.database;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import minigames.database.DataType.PlayerData;
import minigames.ctype.Skill;

import java.util.HashMap;
import java.util.Objects;

import static arc.util.Log.err;

public class Database {
    public final Fi databaseDirectory;
    public final Seq<PlayerData> players;
    public final Seq<Skill<?>> skills;
    private final HashMap<Team, Seq<Skill<?>>> teamSkillSet;
    private final HashMap<UnitType, Seq<Skill<?>>> unitSkillSet;
    private final HashMap<String, Boolean> gameMode;

    public Database() {
        // init
        databaseDirectory = new Fi(Vars.dataDirectory.path() + "/database", Vars.dataDirectory.type());
        databaseDirectory.mkdirs();
        players = Seq.with();
        skills = Seq.with();
        teamSkillSet = new HashMap<>();
        unitSkillSet = new HashMap<>();
        gameMode = new HashMap<>();

        // game mode setup
        gameMode.put("shuffle", false);
        gameMode.put("marathon", false);
        gameMode.put("duel", false);
        gameMode.put("starlike", false);
        gameMode.put("story", false);
    }

    public void loadSkillList() {

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

    public Seq<Skill<?>> skillSeq(Team team) {
        if(teamSkillSet.get(team) == null) teamSkillSet.put(team, Seq.with());

        return teamSkillSet.get(team);
    }

    public Seq<Skill<?>> skillSeq(UnitType type) {
        if(unitSkillSet.get(type) == null) unitSkillSet.put(type, Seq.with());

        return unitSkillSet.get(type);
    }
}
