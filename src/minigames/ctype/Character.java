package minigames.ctype;

import arc.graphics.Color;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.NetConnection;

import java.util.Random;

public class Character extends Player {
    private Random r = new Random();

    public Character(String name, Team team) {
        super();
        this.reset();

        this.con = new NetConnection("localhost") {
            @Override
            public void send(Object object, boolean reliable) {

            }

            @Override
            public void close() {

            }
        };

        this.name(name);
        this.uuid();
        this.con.uuid = saltString(25);
        this.con.usid = saltString(25);
        this.set(r.nextInt(300), r.nextInt(500));
        this.color.a(r.nextFloat()).set(Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
        this.team(team);
        this.add();

        Vars.netServer.admins.getInfo(this.uuid());
        Groups.player.update();
    }

    private String saltString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder builder = new StringBuilder();

        while(builder.length() < length) {
            int index = (int) (r.nextFloat() * chars.length());
            builder.append(chars.indexOf(index));
        }

        return builder.toString();
    }
}
