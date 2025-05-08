package wandweaver.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wandweaver.utils.Sounds;
import wandweaver.utils.SpecialItem;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
    @Shadow public abstract void equipStack(EquipmentSlot slot, ItemStack stack);

    @Inject(at = @At("HEAD"), method = "dropItem", cancellable = true)
    private void wandWeaver$voidWand(ItemStack stack, boolean dropAtSelf, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (!SpecialItem.isSpecial(stack)) {
            return;
        }

        if (!(wandWeaver$this() instanceof PlayerEntity player)) {
            return;
        }

        Sounds.playSoundOnPlayer(player, SoundEvents.ENTITY_PLAYER_TELEPORT);
        cir.setReturnValue(null);
    }

    @Inject(at = @At("HEAD"), method = "swapHandStacks")
    private void wandWeaver$voidWandSwap(CallbackInfo ci) {
        ItemStack offhand = getEquippedStack(EquipmentSlot.OFFHAND);
        ItemStack mainhand = getEquippedStack(EquipmentSlot.MAINHAND);

        if (!SpecialItem.isSpecial(mainhand)) {
            return;
        }

        this.equipStack(EquipmentSlot.MAINHAND, offhand);
        mainhand.setCount(0);
    }


    @Unique
    private LivingEntity wandWeaver$this() {
        return ((LivingEntity)(Object)this);
    }
}
