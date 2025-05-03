package wandweaver.spells.context.utilities;

import net.minecraft.block.Block;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.data.IBlockConversionData;
import wandweaver.spells.context.data.IItemConversionData;

import java.util.List;

public interface IBlockConversionUtilities {
    boolean canConvert(Block target, IBlockConversionData data);
    void convert(Block target, BlockPos targetPos, IBlockConversionData data);
    void convert(Block target, BlockPos targetPos, IBlockConversionData data, @Nullable SoundEvent defaultSoundOnConversion);
    boolean attemptConversion(Block target, BlockPos targetPos, List<IBlockConversionData> data);
    boolean attemptConversion(Block target, BlockPos targetPos, List<IBlockConversionData> data, @Nullable SoundEvent defaultSoundOnConversion);
}
