package wandweaver.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wandweaver.utils.SpecialItem;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    private void wandWeaver$avoidSpecialItemInteraction(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = this.getStackInHand(hand);

        if (SpecialItem.isSpecial(stack)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Unique
    private PlayerEntity wandWeaver$this() {
        return (PlayerEntity) (Object) this;
    }
}
