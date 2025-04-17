package wandweaver.utils;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.ArrayList;
import java.util.List;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    public static final PacketCodec<PacketByteBuf, List<Direction>> DIRECTION_LIST_CODEC =
            PacketCodec.of(
                    (List<Direction> directions, PacketByteBuf buf) -> {
                        buf.writeInt(directions.size());

                        for (Direction direction : directions) {
                            buf.writeEnumConstant(direction);
                        }
                    },
                    (PacketByteBuf buf) -> {
                        int size = buf.readInt();

                        List<Direction> directions = new ArrayList<>(size);

                        for (int i = 0; i < size; i++) {
                            directions.add(buf.readEnumConstant(Direction.class));
                        }

                        return directions;
                    }
            );

    public float getZPositiveRotationAxis() {
        float value = 0;

        switch(this) {
            case UP -> value = 0;
            case DOWN -> value = 180;
            case LEFT -> value = 90;
            case RIGHT -> value = 270;
        }

        return (float) Math.toRadians(value);
    }


}
