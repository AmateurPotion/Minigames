package minigames.io.commands;

import arc.Core;
import arc.func.Cons2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.UnitCommandCallPacket;
import mindustry.net.NetConnection;
import mindustry.type.UnitType;
import minigames.type.dataType.PacketListener;
import minigames.type.dataType.PlayerData;
import minigames.utils.*;
import minigames.modes.marathon.Marathon;

import java.util.MissingResourceException;
import java.util.Objects;

import static arc.util.Log.err;
import static mindustry.Vars.*;
import static minigames.Entry.db;
import static minigames.modes.marathon.Marathon.updateScore;

public class CommandLoader {

    public void registerServerCommands(CommandHandler handler) {
        handler.register("ts", "Team random shuffle",  args -> TeamManager.shuffle());

        handler.register("put", "<key> <value> <string/int/bool>", "set settings", args -> {
            if(args.length == 2) {
                Core.settings.put(args[0], args[1]);
                Log.info("put value "+ args[1] + " to " + args[0]);
            } else if(args.length == 3) {
                switch(args[2]) {
                    case "string":
                        Core.settings.put(args[0], args[1]);
                        break;
                    case "int":
                        if(args[1].matches("[+-]?\\d*(\\.\\d+)?")) {
                            Core.settings.put(args[0], Integer.parseInt(args[1]));
                        } else {
                            err("not int");
                        }
                        break;
                    case "bool":
                        if(Objects.equals(args[1], "true") || Objects.equals(args[1], "false")) {
                            Core.settings.put(args[0], Boolean.parseBoolean(args[1]));
                        } else {
                            err("not bool");
                        }
                        break;
                    default:
                        err("string/int/bool");
                        break;
                }
                Log.info("put value "+ args[1] + " to " + args[0]);
            }
        });

        handler.register("read", "<key>", "read settings", args -> {
            if(args.length > 0) {
                Log.info(Core.settings.getString(args[0], "null"));
            }
        });

        handler.register("del", "<key>", "delete settings key", args -> {
            if(args.length > 0) {
               Core.settings.remove(args[0]);
            }
        });

        handler.register("team", "<playerName> <teamCode>", "change player team", args -> {
            if(args.length > 1) {
                Player target = Groups.player.find(player -> Objects.equals(player.name, args[0]));
                if(target != null) {
                    if(intCheck(args[1]) && Integer.parseInt(args[1]) > -1 && Integer.parseInt(args[1]) < 256) {
                        target.team(Team.all[Integer.parseInt(args[1])]);
                        Log.info(Team.all[Integer.parseInt(args[1])].name);
                    } else {
                        err("can't find team!");
                    }
                } else {
                    err("can't find player!");
                }
            } else {
                err("need two params");
            }
        });

        handler.register("changeUnit", "<player> <unitName>", "", args -> {
            if(args.length > 1) {
                Player target = Groups.player.find(p -> Objects.equals(p.name, args[0]));
                if(target != null) {
                    UnitType unit = content.units().find(u -> Objects.equals(u.name, args[1]));
                    if(unit != null) {
                        PlayerManager.changeUnit(target, unit);
                    } else {
                        err("can't find unit!");
                    }
                } else {
                    err("can't find player!");
                }
            }
        });

        handler.register("marathon", "", args -> {
            if(state.is(GameState.State.playing)){
                err("Already hosting. Type 'stop' to stop hosting first.");
            } else {
                Marathon.startMarathon();
            }
        });

        handler.register("saveAll", "", args -> {
            db.players.each( data -> {
                Log.info("save data - " + data.player.name());
                db.savePlayerData(data);
            });
        });

        handler.register("perm", "<name> <level>", "", args -> {
            if(args.length == 2) {
                if(intCheck(args[1])) {
                    PlayerData data = db.find(args[0]);
                    data.permissionLevel(Integer.parseInt(args[1]));
                }
            } else if(args.length == 1) {
                Log.info(db.find(args[0]).permissionLevel());
            }
        });

        handler.register("test", "", args -> {
            
        });
    }

    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("join", "Join to current game", (args, player) -> {
            if(db.gameMode("marathon")) {
                PlayerData data = db.players.find(p -> p.player == player);
                if(data.config.getBool("joinAllow@NS", true)) {
                    Marathon.playerJoin(data);
                    data.config.put("joinAllow@NS", false);
                }
                Call.setHudText(player.con, "score : " + data.config.getInt("score", 0));
            }
        });

        handler.<Player>register("spec", "change to spectate mode", (args, player) -> {
            if(player.team() != Team.derelict) {
                PlayerData data = db.players.find(p -> p.player == player);
                data.config.put("line@NS", 1);
                data.config.put("joinAllow@NS", true);
                if(player.unit() != null)Call.unitDespawn(player.unit());
            }
        });

        handler.<Player>register("respawn", "respawn", (args, player) -> {
            if(player.team() != Team.derelict && db.gameMode("marathon")) {
                PlayerData data = db.players.find(p -> p.player == player);
                if(!data.config.getBool("joinAllow@NS", true)) {
                    Marathon.respawn(data);
                    updateScore(data, -200);
                }
            }
        });

        handler.<Player>register("scoreboard", "open scoreboard", (args, player) -> {
            if(db.gameMode("marathon")) {
                Call.infoMessage(player.con, Marathon.scoreboard());
            }
        });

        handler.<Player>register("skills", "open skill info", (args, player) -> {
            PlayerData data = db.find(player);
            if(data != null) {
                Seq<String> skills = db.currentSkills(data);
                if (skills.size > 0) {
                    String[] skillList = new String[]{""};
                    try {
                        Seq<String> tNames = Seq.with();
                        skills.each(name -> tNames.add(db.bundle.getString(player, "skill." + name + ".name")));
                        tNames.each(name -> skillList[0] += name + "\n");
                        db.menu.sendTempMenu(player, db.bundle.getString(player, "skill.status.title"), skillList[0], new String[][]{tNames.toArray()}, (p, option) -> {
                            Call.menu(p.con(), 0, db.bundle.getString(p, "skill." + skills.get(option) + ".name"), db.bundle.getString(p, "skill." + skills.get(option) + ".description"), new String[][]{{db.bundle.getString(p, "ok")}});
                        });
                    } catch (MissingResourceException e) {
                        Log.info(e.getLocalizedMessage());
                    }
                }
            }
        });

        handler.<Player>register("skill", "<skillName>", "open skill info", (args, player) -> {
            if(args.length == 1) {
                try {
                    Call.menu(player.con(), 0, db.bundle.getString(player, "skill." + args[0] + ".name"), db.bundle.getString(player, "skill." + args[0] + ".description"), new String[][]{{db.bundle.getString(player, "ok")}});
                } catch (MissingResourceException e) {
                    Log.info(e.getLocalizedMessage());
                }
            }
        });
    }

    private boolean intCheck(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}