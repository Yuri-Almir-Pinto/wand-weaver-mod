package wandweaver.spells.context.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.state.ISacrificedItemsState;
import wandweaver.spells.context.utilities.*;

public record SpellCastingContext(
        ServerPlayerEntity player,
        ISoundUtilities sound,
        IInteractionUtilities interaction,
        ITargetingUtilities targeting,
        IBlockConversionUtilities blockConversion,
        IItemConversionUtilities itemConversion,
        IEntityUtilities entity,
        ISacrificedItemsState sacrificedItems) implements ISpellCastingContext {
}
