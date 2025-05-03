package wandweaver.spells.context.impl.data;

import net.minecraft.block.Block;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.data.IBlockConversionData;

public record BlockConversionData(
        Block from,
        Block to,
        @Nullable SoundEvent soundOnConversion
        ) implements IBlockConversionData {
    public BlockConversionData(Block from, Block to) {
        this(from, to, null);
    }
}
