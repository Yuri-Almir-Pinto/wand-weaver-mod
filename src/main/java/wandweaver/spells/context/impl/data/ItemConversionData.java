package wandweaver.spells.context.impl.data;

import net.minecraft.item.Item;
import wandweaver.spells.context.data.IItemConversionData;

public record ItemConversionData(
        Item from,
        Item to,
        int cost,
        int resultAmount,
        boolean greedyConversion
) implements IItemConversionData {
    public ItemConversionData(Item from, Item to, int cost, int resultAmount) {
        this(from, to, cost, resultAmount, false);
    }

    public ItemConversionData(Item from, Item to, boolean greedyConversion) {
        this(from, to, 1, 1, greedyConversion);
    }

    public ItemConversionData(Item from, Item to) {
        this(from, to, false);
    }
}
