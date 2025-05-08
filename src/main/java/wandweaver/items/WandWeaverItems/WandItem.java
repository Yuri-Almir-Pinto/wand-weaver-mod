package wandweaver.items.WandWeaverItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wandweaver.WandWeaver;
import wandweaver.items.ItemsManager;
import wandweaver.utils.Sounds;

public class WandItem extends Item {
    public WandItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return Integer.MAX_VALUE;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand == Hand.OFF_HAND) {
            return ActionResult.PASS;
        }

        if (user.getWorld().isClient) {
            WandWeaver.isCasting = true;
        }

        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user.getWorld().isClient) {
            WandWeaver.isCasting = false;
        }

        return (ItemStack) (Object) this;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user.getWorld().isClient) {
            WandWeaver.isCasting = false;
        }

        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean deleted = false;
        stack.setCount(2);

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);

            if (itemStack.isOf(ItemsManager.WAND) && itemStack.getCount() != 2) {
                itemStack.setCount(0);
                deleted = true;
            }
        }

        stack.setCount(1);

        if (!world.isClient && deleted) {
            Sounds.playSoundOnPlayer(player, SoundEvents.ENTITY_PLAYER_TELEPORT);
        }
    }
}
