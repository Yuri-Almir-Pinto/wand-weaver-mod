package wandweaver.spells.context.impl.data;

import net.minecraft.block.Block;
import wandweaver.spells.context.data.IBlockConversionData;

public record BlockConversionData(
        Block from,
        Block to
) implements IBlockConversionData {
}
