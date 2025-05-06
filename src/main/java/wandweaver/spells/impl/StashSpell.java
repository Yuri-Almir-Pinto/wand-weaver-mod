package wandweaver.spells.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;

import java.util.List;

public class StashSpell extends AbstractSpell {
    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.DOWN,
                Direction.RIGHT,
                Direction.UP,
                Direction.LEFT
        );
    }

    @Override
    public Text getName(@Nullable List<Direction> directions) {
        return Text.translatable("spell.name.stash");
    }

    @Override
    public Text getDescription(@Nullable List<Direction> directions) {
        return Text.translatable("spell.description.stash");
    }

    @Override
    public String getIdentifier() {
        return "stash";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        NamedScreenHandlerFactory screenHandlerFactory = getNamedScreenHandlerFactory(context);

        context.player().openHandledScreen(screenHandlerFactory);
        context.sound().playSoundOnPlayer(SoundEvents.BLOCK_ENDER_CHEST_OPEN);
    }

    private NamedScreenHandlerFactory getNamedScreenHandlerFactory(ISpellCastingContext context) {
        ServerPlayerEntity player = context.player();
        EnderChestInventory enderChestInventory = player.getEnderChestInventory();

        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.translatable("container.enderchest");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X3,
                        syncId,
                        playerInventory,
                        enderChestInventory,
                        3) {
                    @Override
                    public boolean canUse(PlayerEntity player) {
                        return true;
                    }

                    @Override
                    public void onClosed(PlayerEntity player) {
                        super.onClosed(player);

                        context.sound().playSoundOnPlayer(SoundEvents.BLOCK_ENDER_CHEST_CLOSE);
                    }
                };
            }
        };
    }
}
