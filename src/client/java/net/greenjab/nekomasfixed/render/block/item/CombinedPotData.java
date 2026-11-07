package net.greenjab.nekomasfixed.render.block.item;

import net.greenjab.nekomasfixed.registry.other.PotEngravingDecoration;
import net.greenjab.nekomasfixed.registry.other.PotFaceDecoration;
import net.minecraft.world.level.block.entity.PotDecorations;

import java.util.List;

/** Everything the pot renderer draws, read from either a placed pot or a pot item. */
public record CombinedPotData(
        PotDecorations decorations,
        PotEngravingDecoration engraving,
        PotFaceDecoration face,
        List<Boolean> glow
) {}
