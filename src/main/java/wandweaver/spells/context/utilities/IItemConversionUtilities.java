package wandweaver.spells.context.utilities;

import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.data.IItemConversionData;

import java.util.List;

public interface IItemConversionUtilities {
    boolean canConvert(IItemConversionData data);
    void convert(IItemConversionData data);
    void convert(IItemConversionData data, @Nullable SoundEvent defaultSoundOnConversion);
    boolean attemptConversion(List<IItemConversionData> data);
    boolean attemptConversion(List<IItemConversionData> data, @Nullable SoundEvent defaultSoundOnConversion);
}
