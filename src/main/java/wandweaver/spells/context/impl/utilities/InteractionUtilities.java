package wandweaver.spells.context.impl.utilities;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import wandweaver.spells.context.utilities.IInteractionUtilities;
import wandweaver.utils.EphemeralItems;

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
            return this.interactBlockAsIfHolding(itemStack);
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return this.interactEntityAsIfHolding(itemStack);
        } else {
            return this.interactItemAsIfHolding(itemStack);
        }
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
        ItemStack wandItem = this.player.getMainHandStack();

        this.player.getInventory().setSelectedStack(EphemeralItems.turnEphemeral(itemStack));

        boolean originalSneaking = player.isSneaking();

        // Forces the player to be sneaking, to avoid any interaction that isn't the casting of a spell, since the
        // casting of a spell is certainly the desired action if the player is currently casting one.
        this.player.setSneaking(true);

        boolean result = func.get();

        this.player.getInventory().setSelectedStack(wandItem);

        this.player.setSneaking(originalSneaking);

        itemStack.setCount(0);

        return result;
    }


}
