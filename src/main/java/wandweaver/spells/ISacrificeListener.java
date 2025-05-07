package wandweaver.spells;

import net.minecraft.item.Item;
import wandweaver.spells.context.ISpellCastingContext;
import java.util.Set;

public interface ISacrificeListener {
    Set<Item> acceptedItems();
    void onSacrifice(ISpellCastingContext context, Item sacrificed);
}
