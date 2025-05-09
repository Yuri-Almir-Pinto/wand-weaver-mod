package wandweaver.spells.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.ISacrificeListener;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.ISpellQueryContext;
import wandweaver.utils.Direction;

import java.util.List;
import java.util.Set;

public class StashSpell extends AbstractSpell implements ISacrificeListener {
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
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.stash");
    }

    @Override
    public int getColor(@Nullable List<Direction> pattern) {
        return 0xb81a9d; // Ender-like shade of pink
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.stash");
    }

    @Override
    public String getIdentifier() {
        return "stash";
    }

    @Override
    public boolean isUnlocked(ISpellQueryContext context) {
        return context.sacrificedItems().hasItem(Items.ENDER_CHEST);
    }

    @Override
    public Set<Item> acceptedItems() {
        return Set.of(Items.ENDER_CHEST);
    }

    @Override
    public void onSacrifice(ISpellCastingContext context, Item sacrificed) {
        if (sacrificed != Items.ENDER_CHEST) {
            return;
        }

        context.player().sendMessage(
                Text.translatable("spell.text.stash.unlocked").styled(s -> s.withColor(Colors.YELLOW))
        );

        context.sound().playSoundOnPlayer(SoundEvents.BLOCK_ENDER_CHEST_CLOSE);
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
