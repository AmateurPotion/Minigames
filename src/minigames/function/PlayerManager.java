package minigames.function;

import arc.Core;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.ctype.ContentType;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

public class PlayerManager {

    public static Unit changeUnit(Player player, UnitType unit) {
        Unit target = unit.spawn(player.team(), player);
        if(Core.settings.getBool("marathon", false)) {

            if(target.health() > 1000) target.health(target.health() / 20);
            if(target.health() > 500) target.health(target.health() / 2);

            if(target.type.flying)target.apply(StatusEffects.tarred, Float.MAX_VALUE);
        }
        if(player.unit() != null) Call.unitDespawn(player.unit());
        player.unit(target);
        return target;
    }

    public static void setPosition(Player player, float x, float y) {
        if(player.unit().type() != null) {
            Unit target = player.unit().type().spawn(player.team(), x, y);
            target.health(player.unit().health());
            target.ammo(player.unit().ammo());
            Vars.content.statusEffects().each(s -> {
                if(player.unit().hasEffect(s)) target.apply(s);
            });
            if(target.type.flying && Core.settings.getBool("marathon", false)) target.apply(StatusEffects.tarred, Float.MAX_VALUE); //공중 너프
            Call.unitDespawn(player.unit());
            player.unit(target);
        }
    }
}
