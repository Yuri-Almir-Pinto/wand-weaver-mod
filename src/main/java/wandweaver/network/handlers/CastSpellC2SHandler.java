package wandweaver.network.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import wandweaver.WandWeaver;
import wandweaver.network.payloads.CastSpellC2SPayload;
import wandweaver.spells.ISpell;
import wandweaver.spells.SpellManager;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.ISpellQueryContext;

public class CastSpellC2SHandler {
    public static void handle(CastSpellC2SPayload payload, ServerPlayNetworking.Context context) {
        ISpell spell = SpellManager.getSpellById(payload.spellId());
        ISpellCastingContext spellcastingContext = SpellManager.getSpellCastingContext(context.player());
        ISpellQueryContext spellQueryContext = SpellManager.toSpellQueryContext(spellcastingContext);

        if (spell != null) {
            if (!spell.isUnlocked(spellQueryContext)) {
                return;
            }

            spell.playerCast(spellcastingContext, payload.pattern());
        }
        else {
            WandWeaver.LOGGER.error("Could not find spell with id '{}'", payload.spellId());
        }
    }
}
