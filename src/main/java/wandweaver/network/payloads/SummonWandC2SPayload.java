package wandweaver.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wandweaver.WandWeaver;

public class SummonWandC2SPayload implements CustomPayload {
    public static final Identifier SUMMON_WAND_PAYLOAD_ID = Identifier.of(WandWeaver.MOD_ID, "summon-wand-s2c");
    public static final CustomPayload.Id<SummonWandC2SPayload> ID = new CustomPayload.Id<>(SUMMON_WAND_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SummonWandC2SPayload> CODEC;
    public static final SummonWandC2SPayload PAYLOAD = new SummonWandC2SPayload();

    private SummonWandC2SPayload() {}

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    static {
        CODEC = PacketCodec.unit(PAYLOAD);
    }
}
