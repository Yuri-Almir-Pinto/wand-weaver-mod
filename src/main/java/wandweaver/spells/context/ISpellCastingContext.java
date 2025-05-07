package wandweaver.spells.context;

import net.minecraft.server.network.ServerPlayerEntity;
import wandweaver.spells.context.state.ISacrificedItemsState;
import wandweaver.spells.context.utilities.*;

public interface ISpellCastingContext {
    ServerPlayerEntity player();

    ISoundUtilities sound();

    IInteractionUtilities interaction();

    ITargetingUtilities targeting();

    IBlockConversionUtilities blockConversion();

    IItemConversionUtilities itemConversion();

    IEntityUtilities entity();

    ISacrificedItemsState sacrificedItems();
}
