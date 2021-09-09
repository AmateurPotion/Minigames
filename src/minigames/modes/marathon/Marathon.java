package minigames.modes.marathon;

import arc.math.geom.Position;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.content.UnitTypes;
import mindustry.core.GameState;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.maps.Map;
import minigames.modes.GameMode;
import minigames.type.dataType.PlayerData;
import minigames.type.dataType.Pos;
import minigames.utils.PlayerManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

import static arc.util.Log.err;
import static mindustry.Vars.*;
import static mindustry.Vars.netServer;
import static minigames.Entry.db;

public class Marathon implements GameMode {
    public final Jval config;
    public PlayerData[] scoreboard = new PlayerData[3];
    private final MarathonCommands commands;
    private final MarathonSkills skills;
    private final MarathonEvents events;
    private boolean active = false;
    private final Random random = new Random();
    private int[] xl, yl;

    public Position getStartPoint(int lineNum) {
        return new Pos(xl[lineNum] * 8, yl[lineNum] * 8);
    }

    public Marathon() {
        config = Jval.newObject();
        xl = new int[] {
                15, 134, 134, 15
        };
        yl = new int[] {
                134, 134, 15, 15
        };
        skills = new MarathonSkills();
        commands = new MarathonCommands();
        events = new MarathonEvents();
    }

    public void home(PlayerData data) {
        Position pos = getStartPoint(data.config.getInt("line@NS", 1) - 1);
        PlayerManager.setPosition(data.player, pos.getX(), pos.getY());
    }

    public void playerJoin(PlayerData data) {
        Team t;
        while(true) {
            t = Team.all[3 + new Random().nextInt(252)];
            Team finalT = t;
            if(Groups.player.find(player -> player.team() == finalT) == null) break;
        }
        data.player.team(t);
        data.config.put("line@NS", 1);
        Unit unit = content.units().copy().filter(u -> u != UnitTypes.block).random().spawn(t, 15 * 8, 134 * 8);
        Call.unitControl(data.player, unit);
    }

    public void respawn(PlayerData data) {
        if(active) {
            int r = random.nextInt(100);
            data.config.asObject().forEach(e -> {
                if(e.key.contains("@DR")) {
                    data.config.remove(e.key);
                }
            });
            data.skillSet.each(skill -> skill.name().contains("@DR"), data.skillSet::remove);
            Groups.unit.each(u -> u.team() == data.player.team(), Call::unitDespawn);
            PlayerManager.changeUnit(data.player, content.units().copy().filter(u -> (u != UnitTypes.omura || data.config.getInt("score", 0) < 10000) && u != UnitTypes.block).random());
            home(data);
            if(r > 30) {
                data.skillSet.add(db.skill("missileShots").imitation(null, null, "@DR"));
            }
        }
    }

    public void updateScore(PlayerData data) {
        if(data != null) {
            Call.setHudText(data.player.con, "score : " + data.config.getInt("score", 0));
        }
    }

    public void updateScore(PlayerData data, int variation) {
        if(active && data != null){
            int score = data.config.getInt("score", 0) + variation;
            data.config.put("score", score);
            Call.setHudText(data.player.con, "score : " + score);
            updateScore();
            if(score > 10000) Call.announce(data.player.name() + "님이 " + score + "점을 달성하셨습니다!");
        }
    }

    public void updateScore() {
        db.players.sort(d -> d.config.getInt("score", 0));
        for(int i = 0; i < 3 && i < db.players.size; i++) {
            scoreboard[i] = db.players.get(db.players.size - 1 - i);
        }
    }

    public String scoreboard() {
        return "< ScoreBoard >\n" +
                (scoreboard[0] != null ? "1st " + scoreboard[0].player.name() + "(" + scoreboard[0].config.getInt("score", 0) + ")\n" : "\n")  +
                (scoreboard[1] != null ? "2nd " + scoreboard[1].player.name() + "(" + scoreboard[1].config.getInt("score", 0) + ")\n" : "\n")  +
                (scoreboard[2] != null ? "3rd " + scoreboard[2].player.name() + "(" + scoreboard[2].config.getInt("score", 0) + ")" : "");
    }

    @Override
    public boolean active() {
        try {
            if(state.is(GameState.State.playing)){
                err("Already hosting. Type 'stop' to stop hosting first.");
                return false;
            } else if(!active) {
                logic.reset();
                Map loadMap = maps.all().find(map -> Objects.equals(map.name(), "Marathon"));
                world.loadMap(loadMap, loadMap.applyRules(Gamemode.survival));
                state.rules = loadMap.applyRules(Gamemode.survival);
                logic.play();
                netServer.openServer();
                if(state.getState() == GameState.State.menu) {
                    return false;
                }
                skills.active();
                commands.active();
                events.active();
                content.units().find(unitType -> unitType == UnitTypes.dagger).abilities.add(new ShieldRegenFieldAbility(20f, 40f, 60f * 4, 60f));

                active = true;
                db.gameMode("solo").active();

                Team.sharded.core().health(Float.MAX_VALUE);
                active = true;
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.info(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void disable() {
        skills.disable();
        commands.disable();
        events.disable();
        active = false;
    }

    @Override
    public void load() {
        skills.load();
        commands.load();
        events.load();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String name() {
        return "marathon";
    }

    @Override
    public @NotNull Jval config() {
        return config;
    }
}
