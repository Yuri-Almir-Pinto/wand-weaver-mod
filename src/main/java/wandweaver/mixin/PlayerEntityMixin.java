package wandweaver.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wandweaver.items.ItemsManager;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    // Disallows interactions with entities using the wand, to avoid creating a wand item in the world.
    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    private void wandWeaver$disallowInteraction(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (hand == Hand.OFF_HAND) {
            return;
        }

        ItemStack mainHand = ((PlayerEntity)(Object)this).getStackInHand(hand);

        if (mainHand.isOf(ItemsManager.WAND)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
