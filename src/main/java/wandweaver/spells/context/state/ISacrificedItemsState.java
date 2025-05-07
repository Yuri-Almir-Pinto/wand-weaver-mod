package wandweaver.spells.context.state;

import net.minecraft.item.Item;
import wandweaver.spells.ISacrificeListener;
import java.util.Set;

public interface ISacrificedItemsState {
    boolean canSacrificeItem(Item item);
    boolean putItem(Item item);
    boolean hasItem(Item item);
    Set<Item> allowedItems();
    Set<ISacrificeListener> getListeners();
}
