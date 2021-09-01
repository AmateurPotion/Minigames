package minigames.utils;

import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.ui.Menus;
import minigames.type.dataType.PlayerData;
import minigames.modes.marathon.Marathon;

import java.util.HashMap;

import static minigames.Entry.db;

public class MenuManager {
    private static final Menus menus = new Menus();
    private int tempMenuNum;
    private int menuNum;
    private final Seq<Integer> excep;
    private final HashMap<String, Integer> menuNames;

    public MenuManager() {
        // init
        tempMenuNum = -1;
        menuNum = 1;
        menuNames = new HashMap<>();
        excep = Seq.with();

        // register menu
        // marathon
        // join
        register("marathon_join", (player, option) -> {
            switch (option) {
                case 0:
                    if(db.gameMode("marathon")) {
                        PlayerData data = db.players.find(p -> p.player == player);
                        if(data.config.getBool("joinAllow@NS", true)) {
                            Marathon.playerJoin(data);
                            data.config.put("joinAllow@NS", false);
                        }
                        Call.setHudText(player.con, "score : " + data.config.getInt("score", 0));
                    }
                    break;
                case -1:
                case 1:
                    if(player.team() != Team.derelict) {
                        PlayerData data = db.players.find(p -> p.player == player);
                        data.config.put("line@NS", 1);
                        data.config.put("joinAllow@NS", true);
                        if(player.unit() != null)Call.unitDespawn(player.unit());
                    }
                    break;
            }
        });
        
        
    }

    public void register(String name, int index, Menus.MenuListener listener) {
        menuNames.put(name, index);
        Menus.registerMenu(index, listener);
        excep.add(index);
    }

    public int register(String name, Menus.MenuListener listener) {
        while (excep.contains(menuNum)) {
            menuNum++;
            if(!excep.contains(menuNum)) break;
        }
        register(name, menuNum, listener);
        menuNum++;
        return menuNum - 1;
    }

    public int get(String name) {
        return menuNames.getOrDefault(name, 0);
    }

    public void sendTempMenu(Player player, String title, String message, String[][] options, Menus.MenuListener listener) {
        int n = tempMenuNum;
        Menus.MenuListener ml = (p, option) -> {
            listener.get(p, option);
            try {
                IntMap<Menus.MenuListener> menuListeners = Synthesis.invokeT(menus, "menuListeners");
                if(menuListeners != null) {
                    menuListeners.remove(n);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Log.info(e.getLocalizedMessage());
            }
        };
        Menus.registerMenu(tempMenuNum, ml);
        Call.menu(player.con, tempMenuNum, title, message, options);
        tempMenuNum--;
    }
}
