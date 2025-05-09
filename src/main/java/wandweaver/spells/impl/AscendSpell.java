package wandweaver.spells.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;

import java.util.List;

public class AscendSpell extends AbstractSpell {
    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.DOWN,
                Direction.DOWN,
                Direction.RIGHT,
                Direction.UP,
                Direction.UP,
                Direction.UP,
                Direction.UP
        );
    }

    @Override
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.ascend");
    }

    @Override
    public int getColor(@Nullable List<Direction> pattern) {
        return 0xa3a3a3; // Lighter shade of grey
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.ascend");
    }

    @Override
    public String getIdentifier() {
        return "ascend";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();
        BlockPos playerPos = player.getBlockPos();
        Vec3d centerPos = playerPos.toCenterPos();

        int topYAbovePlayer = world.getTopY(Heightmap.Type.MOTION_BLOCKING, playerPos);

        if (topYAbovePlayer > playerPos.getY()) {
            boolean success = player.teleport(
                    centerPos.getX(),
                    topYAbovePlayer + 1,
                    centerPos.getZ(),
                    true
                    );

            if (success) {
                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_PLAYER_TELEPORT);
            }

        }
    }
}
