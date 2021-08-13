package minigames.function;

import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

public class PlayerManager {

    public static void changeUnit(Player player, UnitType unit) {
        Unit target = unit.spawn(player.team(), player);
        if(player.unit() != null) Call.unitDespawn(player.unit());
        player.unit(target);
    }

    public static void setPosition(Player player, float x, float y) {
        if(player.unit().type() != null) {
            Unit target = player.unit().type().spawn(player.team(), x, y);
            target.health(player.unit().health());
            target.ammo(player.unit().ammo());
            Call.unitDespawn(player.unit());
            Call.unitControl(player, target);
        }
    }
}// say ./join to start
