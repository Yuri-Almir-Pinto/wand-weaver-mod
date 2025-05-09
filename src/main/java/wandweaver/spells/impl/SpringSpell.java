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
    public int getColor(@Nullable List<Direction> pattern) {
        return 0x42ECF5; // Aqua shade of blue
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
        boolean shouldReturn;

        shouldReturn = handleExtinguishingPlayerFire(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleExtinguishingTargetEntityFire(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleZombieTransformation(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleOffhandInteractions(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleTargetBlockInteractions(context);
        if (shouldReturn) {
            return;
        }

        handleDefaultWaterBucketInteraction(context);
    }

    private boolean handleExtinguishingPlayerFire(ISpellCastingContext context) {
        if (context.player().isOnFire()) {
            context.player().setFireTicks(0);
            context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return true;
        }
        return false;
    }

    private boolean handleExtinguishingTargetEntityFire(ISpellCastingContext context) {
        Entity hitEntity = context.targeting().getPlayerCrosshairTargetEntity();
        if (hitEntity != null && hitEntity.isOnFire()) {
            hitEntity.setFireTicks(0);
            context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return true;
        }
        return false;
    }

    private boolean handleZombieTransformation(ISpellCastingContext context) {
        Entity hitEntity = context.targeting().getPlayerCrosshairTargetEntity();
        if (hitEntity instanceof ZombieEntity target) {
            EntityType<? extends ZombieEntity> newType = null;
            if (hitEntity instanceof HuskEntity) {
                newType = EntityType.ZOMBIE;
            } else if (hitEntity.getType() == EntityType.ZOMBIE) { // Check explicitly for Zombie
                newType = EntityType.DROWNED;
            }

            if (newType != null) {
                ZombieEntity newZombie = new ZombieEntity(newType, context.player().getWorld());
                boolean successfulConversion = context.entity().transformEntity(target, newZombie);
                if (successfulConversion) {
                    context.sound().playSoundOnPlayer(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleOffhandInteractions(ISpellCastingContext context) {
        ItemStack offhand = context.player().getOffHandStack();
        if (!offhand.isEmpty()) {
            if (offhand.isOf(Items.LAVA_BUCKET)) {
                context.player().setStackInHand(Hand.OFF_HAND, new ItemStack(Items.BUCKET));
                context.player().getInventory().offerOrDrop(new ItemStack(Items.OBSIDIAN, 1));
                context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
                return true;
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
                return true;
            }

            return context
                    .itemConversion()
                    .attemptConversion(ITEM_CONVERSION_DATA, SoundEvents.ITEM_BUCKET_EMPTY);
        }
        return false;
    }

    private boolean handleTargetBlockInteractions(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock(true);
        BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos(true);
        BlockState targetBlockState = context.targeting().getPlayerCrosshairTargetBlockState(true);

        if (targetBlock == Blocks.LAVA && targetBlockState != null && targetBlockPos != null) {
            int lavaLevel = targetBlockState.getFluidState().getLevel();
            BlockState newBlockState = lavaLevel == 8 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.COBBLESTONE.getDefaultState();
            context.player().getServerWorld().setBlockState(targetBlockPos, newBlockState);
            context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return true;
        } else {
            return context
                    .blockConversion()
                    .attemptConversion(targetBlock, targetBlockPos, BLOCK_CONVERSION_DATA, SoundEvents.ITEM_BUCKET_EMPTY);
        }
    }

    private boolean handleDefaultWaterBucketInteraction(ISpellCastingContext context) {
        boolean successfulUsage = context
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
        return successfulUsage; // The method itself returns success/failure
    }
}