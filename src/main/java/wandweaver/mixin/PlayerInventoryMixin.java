package wandweaver.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wandweaver.WandWeaver;
import wandweaver.items.ItemsManager;
import wandweaver.utils.EphemeralItems;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Unique
    private ItemStack wandWeaver$wand = new ItemStack(ItemsManager.WAND, 1);

    @Shadow
    private int selectedSlot;

    @Inject(at = @At("HEAD"), method = "setSelectedSlot", cancellable = true)
    private void wandWeaver$setSlot(int slot, CallbackInfo ci) {
        this.wandWeaver$setHolder(this.wandWeaver$wand);

        if (slot == WandWeaver.WAND_SLOT) {
            this.selectedSlot = WandWeaver.WAND_SLOT;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getSelectedStack", cancellable = true)
    private void wandWeaver$getWand(CallbackInfoReturnable<ItemStack> cir) {
        this.wandWeaver$setHolder(this.wandWeaver$wand);

        if (this.selectedSlot == WandWeaver.WAND_SLOT) {
            cir.setReturnValue(this.wandWeaver$wand);
        }
    }

    @Inject(at = @At("HEAD"), method = "setSelectedStack", cancellable = true)
    private void wandWeaver$refuseSetSlot(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        this.wandWeaver$setHolder(this.wandWeaver$wand);

        if (this.selectedSlot == WandWeaver.WAND_SLOT) {
            if (EphemeralItems.isEphemeral(stack)) {
                ItemStack wand = this.wandWeaver$wand;
                this.wandWeaver$wand = stack;
                cir.setReturnValue(wand);
            }

            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(at = @At("HEAD"), method = "updateItems")
    private void wandWeaver$maintainWand(CallbackInfo ci) {
        if (!this.wandWeaver$wand.isOf(ItemsManager.WAND)) {
            this.wandWeaver$wand = new ItemStack(ItemsManager.WAND, 1);
        }

        if (this.wandWeaver$wand.isEmpty()) {
            this.wandWeaver$wand.setCount(1);
        }
    }

    @Unique
    private void wandWeaver$setHolder(ItemStack stack) {
        if (stack.getHolder() == null) {
            stack.setHolder(((PlayerInventory)(Object) this).player);
        }

    }
}
