package wandweaver.spells.context.data;

import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public interface IItemConversionData {
    Item to();
    Item from();
    int cost();
    int resultAmount();
    boolean greedyConversion();
    @Nullable SoundEvent soundOnConversion();
}
