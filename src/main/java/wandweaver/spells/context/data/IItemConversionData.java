package wandweaver.spells.context.data;

import net.minecraft.item.Item;

public interface IItemConversionData {
    Item to();
    Item from();
    int cost();
    int resultAmount();
    boolean greedyConversion();
}
