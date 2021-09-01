package minigames.io;

import arc.Events;
import arc.graphics.Color;
import arc.util.Log;
import mindustry.ai.formations.patterns.CircleFormation;
import mindustry.content.Fx;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.UnitCommandCallPacket;
import mindustry.input.Binding;
import minigames.type.dataType.PacketListener;

import static minigames.utils.PacketManager.*;

public class PacketLoader {
    public PacketLoader() {}
    public void load() {
        unitCommandCallPackets();
    }

    public void unitCommandCallPackets() {

        // UnitCommandCallPacket

        addPacketListener(new PacketListener<>("unitCommand", UnitCommandCallPacket.class, (con, packet) -> {
            Player player = con.player;
            if(player != null && !player.dead() && player.unit() != null) {
                if(player.unit().isCommanding()){
                    player.unit().clearCommand();
                }else if(player.unit().type.commandLimit > 0){
                    player.unit().commandNearby(new CircleFormation());
                    Call.effect(Fx.commandSend, player.x(), player.y(), player.unit().type.commandRadius, Color.black);
                }
            }
        }));

        addPacketListener(new PacketListener<>("bind-G", UnitCommandCallPacket.class, (con, packet) -> {
            if(con.player != null) {
                Events.fire(new EventList.BindingReceiveEvent(con.player, Binding.command));
            }
        }));
    }
}
