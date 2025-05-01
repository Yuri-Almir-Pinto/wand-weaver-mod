package wandweaver.spells.context.impl.utilities;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import wandweaver.spells.context.utilities.IInteractionUtilities;

import java.util.function.Supplier;

public class InteractionUtilities implements IInteractionUtilities {
    private final ServerPlayerEntity player;
    private final TargetingUtilities targeting;

    public InteractionUtilities(ServerPlayerEntity player, TargetingUtilities targetingUtilities) {
        this.player = player;
        this.targeting = targetingUtilities;
    }

    public boolean interactAsIfHolding(ItemStack itemStack) {
        HitResult hitResult = this.targeting.getPlayerCrosshairTarget();

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            boolean success = this.interactBlockAsIfHolding(itemStack);

            if (success) {
                return success;
            }

            return this.interactItemAsIfHolding(itemStack);
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return this.interactEntityAsIfHolding(itemStack);
        }

        return false;
    }

    public boolean interactEntityAsIfHolding(ItemStack itemStack) {
        return sneakAndSelectItem(itemStack, () -> {
            EntityHitResult hitResult = (EntityHitResult) this.targeting.getPlayerCrosshairTarget();

            return player.interact(
                    hitResult.getEntity(),
                    Hand.MAIN_HAND
            ).isAccepted();
        });
    }

    public boolean interactBlockAsIfHolding(ItemStack itemStack) {
        return sneakAndSelectItem(itemStack, () -> {
            try {
                return this.player.interactionManager.interactBlock(
                        this.player,
                        this.player.getWorld(),
                        itemStack,
                        Hand.MAIN_HAND,
                        (BlockHitResult) this.targeting.getPlayerCrosshairTarget()
                ).isAccepted();
            } catch (ClassCastException ex) {
                // If this exception occured, it means the target is an entity, NOT a block.
                return false;
            }
        });
    }

    public boolean interactItemAsIfHolding(ItemStack itemStack) {
        return sneakAndSelectItem(itemStack, () -> this.player.interactionManager.interactItem(
                    this.player,
                    this.player.getWorld(),
                    itemStack,
                    Hand.MAIN_HAND
            ).isAccepted());
    }

    public boolean sneakAndSelectItem(ItemStack itemStack, Supplier<Boolean> func) {
        ItemStack originalItem = this.player.getMainHandStack();

        this.player.getInventory().setSelectedStack(itemStack);

        boolean originalSneaking = player.isSneaking();

        this.player.setSneaking(false);

        boolean result = func.get();

        this.player.getInventory().setSelectedStack(originalItem);

        this.player.setSneaking(originalSneaking);

        return result;
    }


}
