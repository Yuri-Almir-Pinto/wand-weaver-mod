package wandweaver.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import wandweaver.network.payloads.AnswerSpellS2CPayload;
import wandweaver.networking.handlers.AnswerSpellS2CHandler;

public class WandWeavingClientNetworking {
    public static void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(AnswerSpellS2CPayload.ID, AnswerSpellS2CHandler::handle);
    }

}
