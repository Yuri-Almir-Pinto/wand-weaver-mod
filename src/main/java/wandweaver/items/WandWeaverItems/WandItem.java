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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wandweaver.WandWeaver;
import wandweaver.items.ItemsManager;
import wandweaver.spells.ISpell;
import wandweaver.utils.Direction;
import wandweaver.utils.InscribedWand;
import wandweaver.utils.Sounds;

import java.util.List;

public class WandItem extends Item {
    public WandItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        if (InscribedWand.isInscribed(stack)) {
            ISpell spell = InscribedWand.getSpell(stack);
            List<Direction> pattern = InscribedWand.getInscription(stack);

            if (spell == null || pattern == null) {
                return super.getName(stack);
            }

            return Text.translatable(
                    "item.wand-weaver.wand.name",
                    Text.literal("[").withColor(Colors.GRAY),
                    spell.getName(pattern).withColor(spell.getColor(pattern)),
                    Text.literal("]").withColor(Colors.GRAY)
                    );
        }

        return super.getName(stack);
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

        if (world.isClient) {
            if (InscribedWand.isInscribed(user.getStackInHand(Hand.MAIN_HAND))) {
                WandWeaver.isAutoCasting = true;
            } else {
                WandWeaver.isCasting = true;
            }
        }

        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (world.isClient) {
            if (InscribedWand.isInscribed(user.getStackInHand(Hand.MAIN_HAND))) {
                WandWeaver.isAutoCasting = false;
            } else {
                WandWeaver.isCasting = false;
            }
        }

        return stack;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (world.isClient) {
            if (InscribedWand.isInscribed(user.getStackInHand(Hand.MAIN_HAND))) {
                WandWeaver.isAutoCasting = false;
            } else {
                WandWeaver.isCasting = false;
            }
        }

        return false;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return InscribedWand.isInscribed(stack);
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
