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
import minigames.database.PlayerData;
import minigames.function.PlayerManager;

import java.util.Objects;
import java.util.Random;

import static mindustry.Vars.*;
import static mindustry.Vars.netServer;

public class Marathon {
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
        Vars.content.units().each(pred -> pred.health > 500 , unit -> {
            unit.speed = unit.speed * 2f;
            if(unit.health > 1000) {
                unit.health = unit.health / 1000;
            } else {
                unit.health = unit.health / 2;
            }
        });
        content.units().find(unitType -> unitType == UnitTypes.oct).abilities.each(a -> {
            if(a instanceof ShieldRegenFieldAbility s) {
                s.reload = 3;
            }
        });

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
        Unit unit = content.units().copy().filter(u -> u != UnitTypes.omura).random().spawn(t, 15 * 8, 134 * 8);
        Call.unitControl(data.player, unit);
    }

    public static void respawn(PlayerData data) {
        PlayerManager.changeUnit(data.player, content.units().copy().filter(u -> u != UnitTypes.omura).random());
        data.config.put("score", data.config.getInt("score", 0) - 100);
        Call.setHudText(data.player.con, "score : " + data.config.getInt("score", 0));
        home(data);
    }
}
