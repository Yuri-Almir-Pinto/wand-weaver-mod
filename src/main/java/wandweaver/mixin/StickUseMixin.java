package wandweaver.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wandweaver.WandWeaver;

@Mixin(Item.class)
public abstract class StickUseMixin {
	@Inject(at = @At("HEAD"), method = "getUseAction", cancellable = true)
	private void changeStickToBlock(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
		if (stack.getItem() == Items.STICK) {
			cir.setReturnValue(UseAction.BLOCK);
		}
	}

	@Inject(at = @At("HEAD"), method = "getMaxUseTime", cancellable = true)
	private void changeMaxUseTimeOfStick(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
		if (stack.getItem() == Items.STICK) {
			cir.setReturnValue(Integer.MAX_VALUE);
		}
	}

	@Inject(at = @At("HEAD"), method = "use", cancellable = true)
	private void useStick(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (!(user.getStackInHand(hand).getItem() == Items.STICK)) {
			return;
		}

		if (hand == Hand.OFF_HAND) {
			return;
		}

		if (user.getWorld().isClient) {
			WandWeaver.isCasting = true;
		}

		user.setCurrentHand(hand);
		cir.setReturnValue(ActionResult.CONSUME);
	}

	@Inject(at = @At("HEAD"), method = "finishUsing", cancellable = true)
	private void finishUsingStick(ItemStack stack,
								  World world,
								  LivingEntity user,
								  CallbackInfoReturnable<ItemStack> cir) {
		if (!(user.getStackInHand(Hand.MAIN_HAND).getItem() == Items.STICK)) {
			return;
		}

		if (user.getWorld().isClient) {
			WandWeaver.isCasting = false;
		}

		cir.setReturnValue(stack);
	}

	@Inject(at = @At("HEAD"), method = "onStoppedUsing")
	private void stoppedUsingStick(ItemStack stack,
								   World world,
								   LivingEntity user,
								   int remainingUseTicks,
								   CallbackInfoReturnable<Boolean> cir) {

		if (!(user.getStackInHand(Hand.MAIN_HAND).getItem() == Items.STICK)) {
			return;
		}

		if (user.getWorld().isClient) {
			WandWeaver.isCasting = false;
		}
	}

}