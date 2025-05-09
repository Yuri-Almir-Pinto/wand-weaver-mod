package wandweaver.spells.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;

import java.util.List;

public class CraftSpell extends AbstractSpell {
    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.DOWN,
                Direction.LEFT,
                Direction.UP,
                Direction.RIGHT
        );
    }

    @Override
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.craft");
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.craft");
    }

    @Override
    public String getIdentifier() {
        return "craft";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        ServerPlayerEntity player = context.player();
        NamedScreenHandlerFactory factory = getNamedScreenHandlerFactory(player);

        player.openHandledScreen(factory);
    }

    private static @NotNull NamedScreenHandlerFactory getNamedScreenHandlerFactory(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();

        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.translatable("container.crafting");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new CraftingScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos)) {
                    @Override
                    public boolean canUse(PlayerEntity player) {
                        return true;
                    }
                };
            }
        };
    }
}
