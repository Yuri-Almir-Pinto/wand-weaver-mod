package wandweaver.spells.context.utilities;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import wandweaver.spells.context.data.IBlockConversionData;

import java.util.List;

public interface IBlockConversionUtilities {
    boolean canConvert(Block target, IBlockConversionData data);
    void convert(Block target, BlockPos targetPos, IBlockConversionData data);
    boolean attemptConversion(Block target, BlockPos targetPos, List<IBlockConversionData> data);
}
