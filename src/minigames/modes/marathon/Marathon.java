package minigames.modes.marathon;

import arc.math.geom.Position;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.maps.Map;
import minigames.type.dataType.PlayerData;
import minigames.utils.PlayerManager;

import java.util.Objects;
import java.util.Random;

import static mindustry.Vars.*;
import static mindustry.Vars.netServer;
import static minigames.Entry.db;

public class Marathon {
    private static Random random = new Random();
    public static PlayerData[] scoreboard = new PlayerData[3];
    public static Position start1 = new Position() {
        @Override
        public float getX() {
            return 15 * 8;
        }

        @Override
        public float getY() {
            return 134 * 8;
        }
    },
            start2 = new Position() {
                @Override
                public float getX() {
                    return 134 * 8;
                }

                @Override
                public float getY() {
                    return 134 * 8;
                }
            },
            start3 = new Position() {
                @Override
                public float getX() {
                    return 134 * 8;
                }

                @Override
                public float getY() {
                    return 15 * 8;
                }
            },
            start4 = new Position() {
                @Override
                public float getX() {
                    return 15 * 8;
                }

                @Override
                public float getY() {
                    return 15 * 8;
                }
            };

    public static void startMarathon() {
        content.units().find(unitType -> unitType == UnitTypes.dagger).abilities.add(new ShieldRegenFieldAbility(20f, 40f, 60f * 4, 60f));

        logic.reset();
        Map loadMap = maps.all().find(map -> Objects.equals(map.name(), "Marathon"));
        world.loadMap(loadMap, loadMap.applyRules(Gamemode.survival));
        state.rules = loadMap.applyRules(Gamemode.survival);
        logic.play();
        netServer.openServer();
        db.gameMode("marathon", true);
        db.gameMode("solo", true);

        Team.sharded.core().health(Float.MAX_VALUE);
    }

    public static void home(PlayerData data) {
        switch (data.config.getInt("line@NS", 1)) {
            case 1 -> PlayerManager.setPosition(data.player, Marathon.start1.getX(), Marathon.start1.getY());
            case 2 -> PlayerManager.setPosition(data.player, Marathon.start2.getX(), Marathon.start2.getY());
            case 3 -> PlayerManager.setPosition(data.player, Marathon.start3.getX(), Marathon.start3.getY());
            case 4 -> PlayerManager.setPosition(data.player, Marathon.start4.getX(), Marathon.start4.getY());
        }
    }

    public static void playerJoin(PlayerData data) {
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

    public static void respawn(PlayerData data) {
        if(db.gameMode("marathon")) {
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

    public static void updateScore(PlayerData data) {
        if(data != null) {
            Call.setHudText(data.player.con, "score : " + data.config.getInt("score", 0));
        }
    }

    public static void updateScore(PlayerData data, int variation) {
        if(db.gameMode("marathon") && data != null){
            int score = data.config.getInt("score", 0) + variation;
            data.config.put("score", score);
            Call.setHudText(data.player.con, "score : " + score);
            updateScore();
            if(score > 10000) Call.announce(data.player.name() + "님이 " + score + "점을 달성하셨습니다!");
        }
    }

    public static void updateScore() {
        db.players.sort(d -> d.config.getInt("score", 0));
        for(int i = 0; i < 3 && i < db.players.size; i++) {
            scoreboard[i] = db.players.get(db.players.size - 1 - i);
        }
    }

    public static String scoreboard() {
        return "< ScoreBoard >\n" +
                (scoreboard[0] != null ? "1st " + scoreboard[0].player.name() + "(" + scoreboard[0].config.getInt("score", 0) + ")\n" : "\n")  +
                (scoreboard[1] != null ? "2nd " + scoreboard[1].player.name() + "(" + scoreboard[1].config.getInt("score", 0) + ")\n" : "\n")  +
                (scoreboard[2] != null ? "3rd " + scoreboard[2].player.name() + "(" + scoreboard[2].config.getInt("score", 0) + ")" : "");
    }
}
