package wandweaver.network.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import wandweaver.items.ItemsManager;
import wandweaver.network.payloads.MagicalActionC2SPayload;
import wandweaver.spells.ISpell;
import wandweaver.spells.SpellManager;
import wandweaver.utils.Direction;
import wandweaver.utils.InscribedWand;
import wandweaver.utils.Sounds;
import wandweaver.utils.SpecialItem;

import java.util.List;

public class MagicalActionC2SHandler {
    public static void handle(MagicalActionC2SPayload payload, ServerPlayNetworking.Context context) {
        MagicalActionC2SPayload.Action action = payload.action();
        ServerPlayerEntity player = context.player();
        PlayerInventory inventory = player.getInventory();
        ItemStack stackInMain = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack stackInOff = player.getStackInHand(Hand.OFF_HAND);

        if (action == MagicalActionC2SPayload.Action.ERASE && InscribedWand.isInscribed(stackInMain)) {
            ItemStack result = InscribedWand.uninscribe(stackInMain);

            player.setStackInHand(Hand.MAIN_HAND, result);

            Sounds.playSoundOnPlayer(player, SoundEvents.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER);
            return;
        }

        if (action == MagicalActionC2SPayload.Action.INSCRIBE) {
            List<Direction> pattern = payload.pattern();

            if (pattern.isEmpty()) {
                return;
            }

            if (!stackInMain.isOf(ItemsManager.WAND)) {
                return;
            }

            ISpell spell = SpellManager.getSpellByPattern(pattern);

            if (spell == null) {
                return;
            }

            if (spell.getAutoDrawTime() == -1) {
                player.sendMessage(Text.translatable("spell.action.cannot_autocast")
                        .styled(s -> s.withColor(Colors.LIGHT_RED)), true);
                return;
            }

            ItemStack result = InscribedWand.inscribe(stackInMain, pattern);

            if (result.isEmpty()) {
                return;
            }

            result.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(1f));

            applyRemainderAndCooldown(player, result);

            player.setStackInHand(Hand.MAIN_HAND, result);

            Sounds.playSoundOnPlayer(player, SoundEvents.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER);
            return;
        }

        Tuple<ItemStack, Integer> special = getSpecial(inventory);
        ItemStack specialStack = special.first;
        Integer previousSlot = special.second;

        if (stackInMain.isEmpty()) {
            player.setStackInHand(Hand.MAIN_HAND, specialStack);
        } else if (previousSlot != -1) {
            inventory.setStack(previousSlot, stackInMain.copy());
            stackInMain.setCount(0);
            player.setStackInHand(Hand.MAIN_HAND, specialStack);
        } else {
            int emptySlot = inventory.getEmptySlot();
            if (emptySlot != -1) {
                inventory.setStack(emptySlot, stackInMain.copy());
                player.setStackInHand(Hand.MAIN_HAND, specialStack);
                stackInMain.setCount(0);
            } else {
                if (stackInOff.isEmpty()) {
                    player.setStackInHand(Hand.OFF_HAND, stackInMain.copy());
                    player.setStackInHand(Hand.MAIN_HAND, specialStack);
                    stackInMain.setCount(0);
                } else {
                    return;
                }
            }

        }

        Sounds.playSoundOnPlayer(player, SoundEvents.BLOCK_ENDER_CHEST_CLOSE);
    }

    private static ItemStack applyRemainderAndCooldown(LivingEntity user, ItemStack stack) {
        UseRemainderComponent useRemainderComponent = stack.get(DataComponentTypes.USE_REMAINDER);
        UseCooldownComponent useCooldownComponent = stack.get(DataComponentTypes.USE_COOLDOWN);
        int i = stack.getCount();
        ItemStack itemStack = stack.copy();
        if (useRemainderComponent != null) {
            itemStack = useRemainderComponent.convert(itemStack, i, user.isInCreativeMode(), user::giveOrDropStack);
        }

        if (useCooldownComponent != null) {
            useCooldownComponent.set(stack, user);
        }

        return itemStack;
    }

    private static Tuple<ItemStack, Integer> getSpecial(PlayerInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);

            if (SpecialItem.isSpecial(itemStack)) {
                inventory.setStack(i, ItemStack.EMPTY);
                return new Tuple<>(itemStack, i);
            }
        }

        return new Tuple<>(SpecialItem.getDefault(), -1);
    }

    private record Tuple<T1, T2>(T1 first, T2 second) {}

}
