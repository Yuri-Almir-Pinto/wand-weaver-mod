package wandweaver.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wandweaver.WandWeaver;
import wandweaver.utils.Direction;

import java.util.List;

public record CastSpellC2SPayload(String spellId, List<Direction> pattern) implements CustomPayload {
    public static final Identifier CAST_SPELL_PAYLOAD_ID = Identifier.of(WandWeaver.MOD_ID, "cast-spell-s2c");
    public static final CustomPayload.Id<CastSpellC2SPayload> ID = new CustomPayload.Id<>(CAST_SPELL_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, CastSpellC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    CastSpellC2SPayload::spellId,
                    Direction.DIRECTION_LIST_CODEC,
                    CastSpellC2SPayload::pattern,
                    CastSpellC2SPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}