package wandweaver.spells.impl;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.mixin.AbstractFurnaceBlockEntityAccessor;
import wandweaver.spells.AbstractSpell;
import wandweaver.utils.Direction;

import java.util.List;

public class IgniteSpell extends AbstractSpell {

    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.LEFT,
                Direction.UP,
                Direction.UP,
                Direction.UP,
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN
        );
    }

    @Override
    public Text getName(@Nullable List<Direction> directions) {
        return Text.translatable("spell.name.ignite");
    }

    @Override
    public Text getDescription(@Nullable List<Direction> directions) {
        return Text.translatable("spell.description.ignite");
    }

    @Override
    public String getIdentifier() {
        return "ignite";
    }

    @Override
    public void playerCast(ServerPlayNetworking.Context context, List<Direction> pattern) {
        ServerPlayerEntity player = context.player();

        ItemStack offhandItemStack = player.getOffHandStack();

        if (!offhandItemStack.isEmpty() && offhandItemStack.getItem() == Items.STICK) {
            int stickCount = offhandItemStack.getCount();
            int torchesAmount = Math.clamp(stickCount, 1, 3);

            this.convertItem(context, offhandItemStack, Items.TORCH, torchesAmount);

            this.playSoundOnPlayer(context, SoundEvents.ITEM_FLINTANDSTEEL_USE);
            return;
        }

        boolean driedOffhand = false;

        if (!offhandItemStack.isEmpty() && offhandItemStack.getItem() == Items.POTION) {
            player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT, new ItemStack(Items.GLASS_BOTTLE, offhandItemStack.getCount()));
            driedOffhand = true;
        }

        if (!offhandItemStack.isEmpty() && offhandItemStack.getItem() == Items.WATER_BUCKET) {
            player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT, new ItemStack(Items.BUCKET, offhandItemStack.getCount()));
            driedOffhand = true;
        }

        if (!offhandItemStack.isEmpty() && offhandItemStack.getItem() == Items.WET_SPONGE) {
            player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT, new ItemStack(Items.SPONGE, offhandItemStack.getCount()));
            driedOffhand = true;
        }

        if (driedOffhand) {
            this.playSoundOnPlayer(context, SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return;
        }

        Entity targetEntity = this.getPlayerCrosshairTargetEntity(context);

        if (targetEntity != null && !(targetEntity instanceof CreeperEntity)) {
            targetEntity.setOnFireFor(4);

            this.playSoundOnPlayer(context, SoundEvents.ENTITY_BLAZE_SHOOT);
        }

        BlockEntity targetBlockEntity = this.getPlayerCrosshairTargetBlockEntity(context);

        if (targetBlockEntity instanceof AbstractFurnaceBlockEntity furnaceBlock) {
            AbstractFurnaceBlockEntityAccessor furnaceAccessor = (AbstractFurnaceBlockEntityAccessor) furnaceBlock;
            int extraTime = furnaceBlock instanceof SmokerBlockEntity || furnaceBlock instanceof BlastFurnaceBlockEntity
                    ? 201 : 401;

            if (furnaceAccessor.getLitTimeRemaining() > 0) {
                furnaceAccessor.setLitTimeRemaining(furnaceAccessor.getLitTimeRemaining() + extraTime);
                furnaceAccessor.setLitTotalTime(furnaceAccessor.getLitTotalTime() + extraTime);
            }
            else {
                furnaceAccessor.setLitTimeRemaining(extraTime);
                furnaceAccessor.setLitTotalTime(extraTime);
            }

            player.getServerWorld().setBlockState(
                    targetBlockEntity.getPos(),
                    targetBlockEntity.getCachedState().with(AbstractFurnaceBlock.LIT, true),
                    Block.NOTIFY_ALL
            );

            this.playSoundOnPlayer(context, SoundEvents.ITEM_FLINTANDSTEEL_USE);
            return;
        }

        ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);

        boolean success = this.interactAsIfHolding(context, flintAndSteel);

        if (success) {
            this.playSoundOnPlayer(context, SoundEvents.ITEM_FLINTANDSTEEL_USE);
        }
    }
}
