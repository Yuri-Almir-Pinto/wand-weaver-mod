package wandweaver.spells.impl;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.ISacrificeListener;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;
import java.util.List;

public class SacrificeSpell extends AbstractSpell {
    private static final int MESSAGE_COLOR = 0x960091; // Cool shade of purple

    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.DOWN,
                Direction.LEFT,
                Direction.LEFT,
                Direction.UP
        );
    }

    @Override
    public Text getName(@Nullable List<Direction> directions) {
        return Text.translatable("spell.name.sacrifice");
    }

    @Override
    public Text getDescription(@Nullable List<Direction> directions) {
        return Text.translatable("spell.description.sacrifice");
    }

    @Override
    public String getIdentifier() {
        return "sacrifice";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        MinecraftServer server = context.player().getServer();
        ServerPlayerEntity player = context.player();

        if (server == null) {
            return;
        }

        ItemStack offhand = context.player().getStackInHand(Hand.OFF_HAND);
        Item offhandItem = offhand.getItem();

        if (offhand.isEmpty()) {
            failedMessage(player);
            return;
        }

        if (context.sacrificedItems().hasItem(offhandItem)) {
            failedMessage(player);
            return;
        }

        if (context.sacrificedItems().canSacrificeItem(offhandItem)) {
            offhand.decrement(1);

            context.sacrificedItems().putItem(offhandItem);

            successMessage(player, offhandItem);

            for (ISacrificeListener listener : context.sacrificedItems().getListeners()) {
                listener.onSacrifice(context, offhandItem);
            }

            return;
        }

        failedMessage(player);
    }

    private void successMessage(ServerPlayerEntity player, Item item) {
        player.sendMessage(Text.translatable("spell.text.sacrifice.success_sacrificing", item.getName())
                .styled(style -> style.withColor(MESSAGE_COLOR)), true);
    }

    private void failedMessage(ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("spell.action.cast_failed")
                .styled(style -> style.withColor(MESSAGE_COLOR)), true);
    }
}
