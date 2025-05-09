package wandweaver.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wandweaver.WandWeaver;
import wandweaver.utils.Direction;

import java.util.List;

public record MagicalActionC2SPayload(Action action, List<Direction> pattern) implements CustomPayload {
    public static final Identifier MAGICAL_ACTION_PAYLOAD_ID = Identifier.of(WandWeaver.MOD_ID, "magical-action-s2c");
    public static final CustomPayload.Id<MagicalActionC2SPayload> ID = new CustomPayload.Id<>(MAGICAL_ACTION_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, MagicalActionC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER.xmap(v -> Action.values()[v], Enum::ordinal),
                    MagicalActionC2SPayload::action,
                    Direction.DIRECTION_LIST_CODEC,
                    MagicalActionC2SPayload::pattern,
                    MagicalActionC2SPayload::new
            );

    public static MagicalActionC2SPayload wand() {
        return new MagicalActionC2SPayload(Action.CREATE_WAND, List.of());
    }

    public static MagicalActionC2SPayload inscribe(List<Direction> pattern) {
        return new MagicalActionC2SPayload(Action.INSCRIBE, pattern);
    }

    public static MagicalActionC2SPayload erase() {
        return new MagicalActionC2SPayload(Action.ERASE, List.of());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum Action {
        CREATE_WAND,
        INSCRIBE,
        ERASE
    }
}
