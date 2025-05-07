package wandweaver.spells.context.impl;

import wandweaver.spells.context.ISpellQueryContext;
import wandweaver.spells.context.state.ISacrificedItemsState;

public record SpellQueryContext(
        ISacrificedItemsState sacrificedItems
) implements ISpellQueryContext {
}
