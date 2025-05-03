package wandweaver.spells.context.data;

import net.minecraft.block.Block;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public interface IBlockConversionData {
    Block to();
    Block from();
    @Nullable SoundEvent soundOnConversion();
}
