package wandweaver.network.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import wandweaver.network.payloads.AnswerSpellS2CPayload;
import wandweaver.network.payloads.CheckSpellC2SPayload;
import wandweaver.spells.ISpell;
import wandweaver.spells.SpellManager;
import wandweaver.utils.Direction;

import java.util.List;

public class CheckSpellC2SHandler {
    public static void handle(CheckSpellC2SPayload payload, ServerPlayNetworking.Context context) {
        List<Direction> directions = payload.directions();

        ISpell spell = SpellManager.getSpellByPattern(directions);

        if (spell != null) {
            context.responseSender().sendPacket(
                    new AnswerSpellS2CPayload(spell.getIdentifier())
            );
        }
    }
}
