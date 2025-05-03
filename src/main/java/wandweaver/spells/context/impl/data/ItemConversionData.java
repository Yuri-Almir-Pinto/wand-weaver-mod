package wandweaver.spells.context.impl.data;

import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.data.IItemConversionData;

public record ItemConversionData(
        Item from,
        Item to,
        int cost,
        int resultAmount,
        boolean greedyConversion,
        @Nullable SoundEvent soundOnConversion
) implements IItemConversionData {
    public ItemConversionData(Item from, Item to, int cost, int resultAmount) {
        this(from, to, cost, resultAmount, false, null);
    }

    public ItemConversionData(Item from, Item to, boolean greedyConversion) {
        this(from, to, 1, 1, greedyConversion, null);
    }

    public ItemConversionData(Item from, Item to) {
        this(from, to, false);
    }

    public ItemConversionData(Item from, Item to, SoundEvent soundOnConversion) {
        this(from, to, 1, 1, false, soundOnConversion);
    }

    public ItemConversionData(Item from, Item to, boolean greedyConversion, SoundEvent soundOnConversion) {
        this(from, to, 1, 1, greedyConversion, soundOnConversion);
    }

    public ItemConversionData(Item from, Item to, int cost, int resultAmount, boolean greedyConversion) {
        this(from, to, cost, resultAmount, greedyConversion, null);
    }
}
