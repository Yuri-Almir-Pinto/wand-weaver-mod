package wandweaver.spells.context.impl.utilities;

import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import wandweaver.spells.context.data.IBlockConversionData;
import wandweaver.spells.context.utilities.IBlockConversionUtilities;

import java.util.List;

public class BlockConversionUtilities implements IBlockConversionUtilities {
    public final ServerPlayerEntity player;

    public BlockConversionUtilities(ServerPlayerEntity player) {
        this.player = player;
    }

    public boolean canConvert(Block target, IBlockConversionData data) {
        return target != null && target == data.from();
    }

    public void convert(Block target, BlockPos targetPos, IBlockConversionData data) {
        if (canConvert(target, data)) {
            ServerWorld world = this.player.getServerWorld();
            world.setBlockState(targetPos, data.to().getDefaultState());
        }
    }

    @Override
    public boolean attemptConversion(Block target, BlockPos targetPos, List<IBlockConversionData> data) {
        for (IBlockConversionData conversionData : data) {
            if (canConvert(target, conversionData)) {
                convert(target, targetPos, conversionData);
                return true;
            }
        }

        return false;
    }
}
