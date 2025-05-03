package wandweaver.spells.context.impl.utilities;

import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import wandweaver.spells.context.data.IBlockConversionData;
import wandweaver.spells.context.utilities.IBlockConversionUtilities;
import wandweaver.spells.context.utilities.ISoundUtilities;

import java.util.List;

public class BlockConversionUtilities implements IBlockConversionUtilities {
    public final ServerPlayerEntity player;
    private final ISoundUtilities sound;

    public BlockConversionUtilities(ServerPlayerEntity player, ISoundUtilities sound) {
        this.player = player;
        this.sound = sound;
    }

    public boolean canConvert(Block target, IBlockConversionData data) {
        return target != null && target == data.from();
    }

    public void convert(Block target, BlockPos targetPos, IBlockConversionData data, SoundEvent defaultSoundOnConversion) {
        if (canConvert(target, data)) {
            ServerWorld world = this.player.getServerWorld();
            world.setBlockState(targetPos, data.to().getDefaultState());

            if (data.soundOnConversion() != null) {
                this.sound.playSoundOnPlayer(data.soundOnConversion());
            } else if (defaultSoundOnConversion != null) {
                this.sound.playSoundOnPlayer(defaultSoundOnConversion);
            }
        }
    }

    public void convert(Block target, BlockPos targetPos, IBlockConversionData data) {
        this.convert(target, targetPos, data, null);
    }

    @Override
    public boolean attemptConversion(Block target, BlockPos targetPos, List<IBlockConversionData> data, SoundEvent defaultSoundOnConversion) {
        for (IBlockConversionData conversionData : data) {
            if (canConvert(target, conversionData)) {
                convert(target, targetPos, conversionData, defaultSoundOnConversion);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean attemptConversion(Block target, BlockPos targetPos, List<IBlockConversionData> data) {
        return this.attemptConversion(target, targetPos, data, null);
    }
}
