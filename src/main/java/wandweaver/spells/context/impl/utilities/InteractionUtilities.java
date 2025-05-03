package wandweaver.spells.context.impl.utilities;

import com.jcraft.jorbis.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import wandweaver.spells.context.utilities.IInteractionUtilities;
import wandweaver.spells.context.utilities.ITargetingUtilities;
import wandweaver.utils.EphemeralItems;

import java.util.function.Supplier;

public class InteractionUtilities implements IInteractionUtilities {
    private final ServerPlayerEntity player;
    private final ITargetingUtilities targeting;

    public InteractionUtilities(ServerPlayerEntity player, ITargetingUtilities targetingUtilities) {
        this.player = player;
        this.targeting = targetingUtilities;
    }

    public boolean interactAsIfHolding(ItemStack itemStack) {
        return this.interactAsIfHolding(itemStack, false);
    }

    public boolean interactAsIfHolding(ItemStack itemStack, boolean includeFluids) {
        HitResult hitResult = this.targeting.getPlayerCrosshairTarget(includeFluids);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return this.interactBlockAsIfHolding(itemStack, includeFluids);
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return this.interactEntityAsIfHolding(itemStack);
        } else {
            return this.interactItemAsIfHolding(itemStack);
        }
    }

    public boolean interactEntityAsIfHolding(ItemStack itemStack) {
        return sneakAndSelectItem(itemStack, () -> {
            HitResult hitResult = this.targeting.getPlayerCrosshairTarget();

            if (hitResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }

            EntityHitResult entityHitResult = (EntityHitResult) hitResult;

            return player.interact(
                    entityHitResult.getEntity(),
                    Hand.MAIN_HAND
            ).isAccepted();
        });
    }

    public boolean interactBlockAsIfHolding(ItemStack itemStack) {
        return this.interactBlockAsIfHolding(itemStack, false);
    }

    public boolean interactBlockAsIfHolding(ItemStack itemStack, boolean includeFluids) {
        return sneakAndSelectItem(itemStack, () -> {
            HitResult hitResult = this.targeting.getPlayerCrosshairTarget(includeFluids);

            if (hitResult.getType() != HitResult.Type.BLOCK) {
                return false;
            }

            BlockHitResult blockHitResult = (BlockHitResult) hitResult;

            return this.player.interactionManager.interactBlock(
                    this.player,
                    this.player.getWorld(),
                    itemStack,
                    Hand.MAIN_HAND,
                    blockHitResult
            ).isAccepted();
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

//        // Forces the player to be sneaking, to avoid any interaction that isn't the casting of a spell, since the
//        // casting of a spell is certainly the desired action if the player is currently casting one.
//        this.player.setSneaking(true);

        boolean result = func.get();

        this.player.getInventory().setSelectedStack(wandItem);

        this.player.setSneaking(originalSneaking);

        itemStack.setCount(0);

        return result;
    }


}
