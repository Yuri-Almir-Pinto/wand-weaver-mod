package wandweaver.spells.context.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.state.ISacrificedItemsState;
import wandweaver.spells.context.utilities.*;

public class SpellCastingContext implements ISpellCastingContext {
    private final ServerPlayerEntity player;
    private final ISoundUtilities sound;
    private final IInteractionUtilities interaction;
    private final ITargetingUtilities targeting;
    private final IBlockConversionUtilities blockConversion;
    private final IItemConversionUtilities itemConversion;
    private final IEntityUtilities entity;
    private final ISacrificedItemsState sacrificedItems;

    public SpellCastingContext(
            ServerPlayerEntity player,
            ISoundUtilities sound,
            IInteractionUtilities interaction,
            ITargetingUtilities targeting,
            IBlockConversionUtilities blockConversion,
            IItemConversionUtilities itemConversion,
            IEntityUtilities entity,
            ISacrificedItemsState sacrificedItems
    ) {
        this.player = player;
        this.sound = sound;
        this.interaction = interaction;
        this.targeting = targeting;
        this.blockConversion = blockConversion;
        this.itemConversion = itemConversion;
        this.entity = entity;
        this.sacrificedItems = sacrificedItems;
    }

    @Override
    public ServerPlayerEntity player() {
        return player;
    }

    @Override
    public ISoundUtilities sound() {
        return sound;
    }

    @Override
    public IInteractionUtilities interaction() {
        return interaction;
    }

    @Override
    public ITargetingUtilities targeting() {
        return targeting;
    }

    @Override
    public IBlockConversionUtilities blockConversion() {
        return blockConversion;
    }

    @Override
    public IItemConversionUtilities itemConversion() {
        return itemConversion;
    }

    @Override
    public IEntityUtilities entity() {
        return entity;
    }

    @Override
    public ISacrificedItemsState sacrificedItems() {
        return sacrificedItems;
    }
}
