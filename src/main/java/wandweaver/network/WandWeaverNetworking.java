package wandweaver.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import wandweaver.network.handlers.CastSpellC2SHandler;
import wandweaver.network.handlers.CheckSpellC2SHandler;
import wandweaver.network.handlers.SummonWandC2SHandler;
import wandweaver.network.payloads.AnswerSpellS2CPayload;
import wandweaver.network.payloads.CastSpellC2SPayload;
import wandweaver.network.payloads.CheckSpellC2SPayload;
import wandweaver.network.payloads.SummonWandC2SPayload;

public class WandWeaverNetworking {
    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(CheckSpellC2SPayload.ID, CheckSpellC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnswerSpellS2CPayload.ID, AnswerSpellS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CastSpellC2SPayload.ID, CastSpellC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SummonWandC2SPayload.ID, SummonWandC2SPayload.CODEC);
    }

    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(CheckSpellC2SPayload.ID, CheckSpellC2SHandler::handle);
        ServerPlayNetworking.registerGlobalReceiver(CastSpellC2SPayload.ID, CastSpellC2SHandler::handle);
        ServerPlayNetworking.registerGlobalReceiver(SummonWandC2SPayload.ID, SummonWandC2SHandler::handle);
    }
}
