package wandweaver.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wandweaver.utils.EphemeralItems;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    // Refuses to copy items that have the ephemeral NBT, since they are used only to create effects from the wand, and
    // should not exist in the world itself.
    @Inject(at = @At("HEAD"), method = "copy()Lnet/minecraft/item/ItemStack;", cancellable = true)
    private void wandWeaver$refuseCopy(CallbackInfoReturnable<ItemStack> cir) {
        if (EphemeralItems.isEphemeral(((ItemStack)(Object)this))) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(at = @At("HEAD"), method = "copyWithCount", cancellable = true)
    private void wandWeaver$refuseCopyWithCount(int count, CallbackInfoReturnable<ItemStack> cir) {
        if (EphemeralItems.isEphemeral(((ItemStack)(Object)this))) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

}
