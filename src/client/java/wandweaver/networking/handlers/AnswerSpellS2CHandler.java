package wandweaver.networking.handlers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import wandweaver.network.payloads.AnswerSpellS2CPayload;
import wandweaver.spells.ISpell;
import wandweaver.spells.SpellManager;
import wandweaver.utils.SpellcastingManager;

public class AnswerSpellS2CHandler {
    public static void handle(AnswerSpellS2CPayload payload, ClientPlayNetworking.Context context) {
        ISpell spell = SpellManager.getSpellById(payload.spellId());

        SpellcastingManager.setCurrentSpell(spell);
    }
}
