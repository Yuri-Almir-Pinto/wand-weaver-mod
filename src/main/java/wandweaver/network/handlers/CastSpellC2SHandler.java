package wandweaver.network.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import wandweaver.WandWeaver;
import wandweaver.network.payloads.CastSpellC2SPayload;
import wandweaver.spells.ISpell;
import wandweaver.spells.SpellManager;

public class CastSpellC2SHandler {
    public static void handle(CastSpellC2SPayload payload, ServerPlayNetworking.Context context) {
        ISpell spell = SpellManager.getSpellById(payload.spellId());

        if (spell != null) {
            spell.playerCast(context, payload.pattern());
        }
        else {
            WandWeaver.LOGGER.error("Could not find spell with id '{}'", payload.spellId());
        }
    }
}
