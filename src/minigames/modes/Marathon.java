package minigames.modes;

import arc.Core;
import arc.math.geom.Position;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.maps.Map;
import minigames.Entry;
import minigames.database.PlayerData;
import minigames.function.PlayerManager;

import java.util.Objects;
import java.util.Random;

import static mindustry.Vars.*;
import static mindustry.Vars.netServer;

public class Marathon {
    public static PlayerData[] scoreboard = new PlayerData[3];
    private static boolean changed = false;
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
        Core.settings.put("marathon", true);
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
        PlayerManager.changeUnit(data.player, content.units().copy().filter(u -> (u != UnitTypes.omura || data.config.getInt("score", 0) < 10000) && u != UnitTypes.block).random());
        home(data);
    }

    public static void updateScore(PlayerData data, int variation) {
        if(Core.settings.getBool("marathon", false)){
            int score = data.config.getInt("score", 0) + variation;
            changed = false;
            data.config.put("score", score);
            Call.setHudText(data.player.con, "score : " + data.config.getInt("score", 0));
            updateScore();
        }
    }

    public static void updateScore() {
        Entry.jdb.players.each(d -> {
            for (int i = 0; i < scoreboard.length; i++) {
                if (scoreboard[i] == null) {
                    scoreboard[i] = d;
                    changed = true;
                    break;
                } else if (scoreboard[i].config.getInt("score", 0) < d.config.getInt("score", 0)) {
                    scoreboard[i] = d;
                    changed = true;
                    break;
                } else if(Objects.equals(scoreboard[i].player.name(), d.player.name())) {
                    break;
                }
            }
        });

        if(changed) {
            Call.announce(scoreboard());
        }
    }

    public static String scoreboard() {
        return "< ScoreBoard >\n" +
                (scoreboard[0] != null ? "1st " + scoreboard[0].player.name() + "(" + scoreboard[0].config.getInt("score", 0) + ")\n" : "\n")  +
                (scoreboard[1] != null ? "2nd " + scoreboard[1].player.name() + "(" + scoreboard[1].config.getInt("score", 0) + ")\n" : "\n")  +
                (scoreboard[2] != null ? "3rd " + scoreboard[2].player.name() + "(" + scoreboard[2].config.getInt("score", 0) + ")" : "");
    }
}
