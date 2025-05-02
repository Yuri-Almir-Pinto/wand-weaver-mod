package wandweaver.spells.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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
    public Text getName(@Nullable List<Direction> directions) {
        return Text.translatable("spell.name.ascend");
    }

    @Override
    public Text getDescription(@Nullable List<Direction> directions) {
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

        int topYAbovePlayer = world.getTopY(Heightmap.Type.MOTION_BLOCKING, playerPos);

        if (topYAbovePlayer > playerPos.getY()) {
            player.teleport(
                    playerPos.getX(),
                    topYAbovePlayer + 1,
                    playerPos.getZ(),
                    true
                    );

            context.sound().playSoundOnPlayer(SoundEvents.ENTITY_PLAYER_TELEPORT);
        }
    }
}
