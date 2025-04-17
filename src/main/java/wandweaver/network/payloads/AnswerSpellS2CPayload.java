package wandweaver.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wandweaver.WandWeaver;

public record AnswerSpellS2CPayload(String spellId) implements CustomPayload {
    public static final Identifier ANSWER_SPELL_PAYLOAD_ID = Identifier.of(WandWeaver.MOD_ID, "answer-spell-s2c");
    public static final CustomPayload.Id<AnswerSpellS2CPayload> ID = new CustomPayload.Id<>(ANSWER_SPELL_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, AnswerSpellS2CPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    AnswerSpellS2CPayload::spellId,
                    AnswerSpellS2CPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
