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

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Unique
    private final ItemStack wandWeaver$wand = new ItemStack(ItemsManager.WAND, 1);

    @Shadow
    private int selectedSlot;

    @Inject(at = @At("HEAD"), method = "setSelectedSlot", cancellable = true)
    private void wandWeaver$setSlot(int slot, CallbackInfo ci) {
        if (slot == WandWeaver.WAND_SLOT) {
            this.selectedSlot = WandWeaver.WAND_SLOT;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getSelectedStack", cancellable = true)
    private void wandWeaver$getWand(CallbackInfoReturnable<ItemStack> cir) {
        if (this.selectedSlot == WandWeaver.WAND_SLOT) {
            cir.setReturnValue(this.wandWeaver$wand);
        }
    }

    @Inject(at = @At("HEAD"), method = "setSelectedStack", cancellable = true)
    private void wandWeaver$refuseSetSlot(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (this.selectedSlot == WandWeaver.WAND_SLOT) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
