package minigames.type.dataType;

import arc.func.Cons2;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public record PacketListener<T extends Packet>(String name, Class<T> type, Cons2<NetConnection, T> listener) {
}
