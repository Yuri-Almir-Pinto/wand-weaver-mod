package wandweaver.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wandweaver.WandWeaver;
import wandweaver.utils.Direction;

import java.util.List;

public record CheckSpellC2SPayload(List<Direction> directions) implements CustomPayload {
    public static final Identifier CHECK_SPELL_PAYLOAD_ID = Identifier.of(WandWeaver.MOD_ID, "check-spell-c2s");
    public static final CustomPayload.Id<CheckSpellC2SPayload> ID = new CustomPayload.Id<>(CHECK_SPELL_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, CheckSpellC2SPayload> CODEC =
            PacketCodec.tuple(
                    Direction.DIRECTION_LIST_CODEC,
                    CheckSpellC2SPayload::directions,
                    CheckSpellC2SPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
