package wandweaver.spells.impl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.data.IBlockConversionData;
import wandweaver.spells.context.data.IItemConversionData;
import wandweaver.spells.context.impl.data.BlockConversionData;
import wandweaver.spells.context.impl.data.ItemConversionData;
import wandweaver.utils.Direction;

import java.util.List;

public class SpringSpell extends AbstractSpell {
    private static final List<IItemConversionData> ITEM_CONVERSION_DATA = List.of(
            ItemConversionData.Builder.begin(Items.BUCKET, Items.WATER_BUCKET)
                    .sound(SoundEvents.ITEM_BUCKET_FILL).build(),
            ItemConversionData.Builder.begin(Items.SPONGE, Items.WET_SPONGE)
                    .greedy(true).sound(SoundEvents.BLOCK_SPONGE_ABSORB).build(),
            ItemConversionData.Builder.begin(Items.TORCH, Items.STICK)
                    .greedy(true).cost(4).result(1).greedy(true)
                    .sound(SoundEvents.BLOCK_FIRE_EXTINGUISH).build()
    );

    private static final List<IBlockConversionData> BLOCK_CONVERSION_DATA = List.of(
            BlockConversionData.Builder.begin(Blocks.SPONGE, Blocks.WET_SPONGE)
                    .sound(SoundEvents.BLOCK_SPONGE_ABSORB).build()
    );

    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.RIGHT,
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN,
                Direction.LEFT,
                Direction.LEFT,
                Direction.UP,
                Direction.UP,
                Direction.UP
        );
    }

    @Override
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.spring");
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.spring");
    }

    @Override
    public String getIdentifier() {
        return "spring";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        if (context.player().isOnFire()) {
            context.player().setFireTicks(0);
            context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return;
        }

        Entity hitEntity = context.targeting().getPlayerCrosshairTargetEntity();

        if (hitEntity != null && hitEntity.isOnFire()) {
            hitEntity.setFireTicks(0);
            context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return;
        }

        if (hitEntity instanceof ZombieEntity target) {
            EntityType<? extends ZombieEntity> newType = hitEntity instanceof HuskEntity ? EntityType.ZOMBIE
                    : hitEntity instanceof ZombieEntity ? EntityType.DROWNED
                    : null;

            if (newType != null) {
                ZombieEntity newZombie = new ZombieEntity(newType, context.player().getWorld());
                boolean successfulConversion = context.entity().transformEntity(target, newZombie);
                if (successfulConversion) {
                    context.sound().playSoundOnPlayer(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE);
                    return;
                }
            }
        }

        ItemStack offhand = context.player().getOffHandStack();

        if (!offhand.isEmpty()) {
            if (offhand.isOf(Items.LAVA_BUCKET)) {
                context.player().setStackInHand(Hand.OFF_HAND, new ItemStack(Items.BUCKET));
                context.player().getInventory().offerOrDrop(new ItemStack(Items.OBSIDIAN, 1));
                context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
                return;
            } else if (offhand.isOf(Items.GLASS_BOTTLE)) {
                offhand.decrement(1);
                ItemStack waterBottle = new ItemStack(Items.POTION, 1);
                waterBottle.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.WATER));

                if (offhand.isEmpty()) {
                    context.player().setStackInHand(Hand.OFF_HAND, waterBottle);
                } else {
                    context.player().getInventory().offerOrDrop(waterBottle);
                }

                context.sound().playSoundOnPlayer(SoundEvents.ITEM_BOTTLE_FILL);
                return;
            }

            boolean successfulConversion = context
                    .itemConversion()
                    .attemptConversion(ITEM_CONVERSION_DATA, SoundEvents.ITEM_BUCKET_EMPTY);

            if (successfulConversion) {
                return;
            }
        }

        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock(true);
        BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos(true);
        BlockState targetBlockState = context.targeting().getPlayerCrosshairTargetBlockState(true);

        if (targetBlock == Blocks.LAVA && targetBlockState != null) {
            int lavaLevel = targetBlockState.getFluidState().getLevel();

            BlockState newBlockState = lavaLevel == 8 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.COBBLESTONE.getDefaultState();

            context.player()
                    .getServerWorld()
                    .setBlockState(targetBlockPos, newBlockState);

            context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return;
        } else {
            boolean successfulBlockConversion = context
                    .blockConversion()
                    .attemptConversion(targetBlock, targetBlockPos, BLOCK_CONVERSION_DATA, SoundEvents.ITEM_BUCKET_EMPTY);

            if (successfulBlockConversion) {
                return;
            }
        }


        var successfulUsage = context
                .interaction()
                .interactAsIfHolding(new ItemStack(Items.WATER_BUCKET));

        if (!successfulUsage) {
            successfulUsage = context
                    .interaction()
                    .interactItemAsIfHolding(new ItemStack(Items.WATER_BUCKET));
        }

        if (successfulUsage) {
            context.sound().playSoundOnPlayer(SoundEvents.ITEM_BUCKET_EMPTY);
        }
    }
}
