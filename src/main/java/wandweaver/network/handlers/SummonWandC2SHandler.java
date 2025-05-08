package wandweaver.network.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import wandweaver.network.payloads.SummonWandC2SPayload;
import wandweaver.utils.Sounds;
import wandweaver.utils.SpecialItem;

public class SummonWandC2SHandler {
    public static void handle(SummonWandC2SPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        PlayerInventory inventory = player.getInventory();
        ItemStack stackInMain = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack stackInOff = player.getStackInHand(Hand.OFF_HAND);

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
