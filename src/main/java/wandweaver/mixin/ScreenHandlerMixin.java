package wandweaver.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wandweaver.utils.Sounds;
import wandweaver.utils.SpecialItem;


@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow @Final public DefaultedList<Slot> slots;

    @Inject(at = @At("RETURN"), method = "internalOnSlotClick")
    private void wandWeaver$guaranteeVoidWand(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler self = (ScreenHandler)(Object)this; // Cast to self for calling protected abstract methods

        if (self instanceof CraftingScreenHandler || self instanceof PlayerScreenHandler) {
            return;
        }

        for (Slot slot : this.slots) {
            if (slot.inventory instanceof PlayerInventory) {
                continue;
            }

            ItemStack stack = slot.getStack();

            if (SpecialItem.isSpecial(stack)) {
                stack.setCount(0);
                Sounds.playSoundOnPlayer(player, SoundEvents.ENTITY_PLAYER_TELEPORT);
            }
        }
    }
}