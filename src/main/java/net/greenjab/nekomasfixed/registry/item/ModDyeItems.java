package net.greenjab.nekomasfixed.registry.item;

import net.greenjab.nekomasfixed.util.ModColors;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

// 1.21.1 note: vanilla DyeItem requires a DyeColor, which only has the 16 standard
// colours (no amber/aqua/indigo/maroon). These dyes are plain items: crafting and
// fireworks take them through mixins, and on a sign they set the text colour directly.
public class ModDyeItems extends Item implements SignApplicator {
    public ModDyeItems(Item.Properties settings) {
        super(settings);
    }

    @Override
    public boolean tryApplyToSign(Level level, SignBlockEntity sign, boolean isFront, Player player) {
        int colour = ModColors.fireworkColor(this);
        if (sign.updateText(text -> dyed(text, colour), isFront)) {
            level.playSound(null, sign.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    private static SignText dyed(SignText text, int colour) {
        SignText result = text;
        for (int line = 0; line < SignText.LINES; line++) {
            Component message = result.getMessage(line, false);
            result = result.setMessage(line, message.copy().withStyle(message.getStyle().withColor(colour)));
        }
        return result;
    }
}
