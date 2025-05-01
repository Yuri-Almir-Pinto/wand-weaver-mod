package wandweaver.spells.context.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.utilities.*;

public class SpellCastingContext implements ISpellCastingContext {
    private final ServerPlayerEntity player;
    private final ISoundUtilities sound;
    private final IInteractionUtilities interaction;
    private final ITargetingUtilities targeting;
    private final IBlockConversionUtilities blockConversion;
    private final IItemConversionUtilities itemConversion;

    public SpellCastingContext(
            ServerPlayerEntity player,
            ISoundUtilities sound,
            IInteractionUtilities interaction,
            ITargetingUtilities targeting,
            IBlockConversionUtilities blockConversion,
            IItemConversionUtilities itemConversion
    ) {
        this.player = player;
        this.sound = sound;
        this.interaction = interaction;
        this.targeting = targeting;
        this.blockConversion = blockConversion;
        this.itemConversion = itemConversion;
    }

    public ServerPlayerEntity player() {
        return player;
    }

    public ISoundUtilities sound() {
        return sound;
    }

    public IInteractionUtilities interaction() {
        return interaction;
    }

    public ITargetingUtilities targeting() {
        return targeting;
    }

    public IBlockConversionUtilities blockConversion() {
        return blockConversion;
    }

    public IItemConversionUtilities itemConversion() {
        return itemConversion;
    }
}
