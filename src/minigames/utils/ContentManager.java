package minigames.utils;

import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.gen.Call;
import mindustry.type.ItemStack;
import minigames.modes.marathon.Marathon;
import minigames.type.dataType.PlayerData;

import static minigames.Entry.db;

public class ContentManager {
    public static Seq<ItemStack> refineScore(PlayerData data) {
        Seq<ItemStack> result = Seq.with();
        if(data != null) {
            if(data.config.getInt("refineChance", 0) > 0) {
                int score = data.config.getInt("score", 0) / 2;

                ItemStack stack = new ItemStack(Items.scrap, score);
                result.add(stack);
                String message = Synthesis.findItemIcon(stack.item) + "[#" + stack.item.color.toString() +"](" + stack.amount + ")[]";

                data.addConfigInt("score", -score);
                data.addConfigInt("refineChance", -1);
                result.each(s -> data.addItem(s.item, s.amount));
                Marathon.updateScore(data);
                db.savePlayerData(data);
                Call.infoToast(data.player.con(), db.bundle.getString(data.player, "refine.gainItems", message), 3);
            } else {
                Call.infoToast(data.player.con(), db.bundle.getString(data.player, "refine.noChance"), 2);
            }
        }
        return result;
    }
}
