package wandweaver.spells.impl;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import wandweaver.WandWeaver;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.data.IBlockConversionData;
import wandweaver.spells.context.data.IItemConversionData;
import wandweaver.spells.context.impl.data.BlockConversionData;
import wandweaver.spells.context.impl.data.ItemConversionData;
import wandweaver.utils.Direction;

import java.util.List;

public class GrowthSpell extends AbstractSpell {
    public static final List<IItemConversionData> ITEM_CONVERSION_DATA = List.of(
            ItemConversionData.Builder.begin(Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE).greedy(true)
                    .sound(SoundEvents.BLOCK_SCULK_BREAK).build(),
            ItemConversionData.Builder.begin(Items.COBBLESTONE, Items.MOSSY_COBBLESTONE).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.STONE, Items.MOSS_BLOCK).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.MOSSY_COBBLESTONE, Items.MOSS_BLOCK).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.MOSSY_STONE_BRICKS, Items.MOSS_BLOCK).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.DIRT, Items.GRASS_BLOCK).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.COARSE_DIRT, Items.GRASS_BLOCK).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.DIRT_PATH, Items.GRASS_BLOCK).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            ItemConversionData.Builder.begin(Items.GRASS_BLOCK, Items.MOSS_BLOCK).greedy(true)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build()
    );

    public static final List<IBlockConversionData> BLOCK_CONVERSION_DATA = List.of(
            BlockConversionData.Builder.begin(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.MOSSY_COBBLESTONE, Blocks.MOSS_BLOCK)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.STONE, Blocks.MOSS_BLOCK)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.DIRT, Blocks.GRASS_BLOCK)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.COARSE_DIRT, Blocks.GRASS_BLOCK)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.DIRT_PATH, Blocks.GRASS_BLOCK)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build(),
            BlockConversionData.Builder.begin(Blocks.GRASS_BLOCK, Blocks.MOSS_BLOCK)
                    .sound(SoundEvents.ITEM_BONE_MEAL_USE).build()
    );

    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.UP,
                Direction.UP,
                Direction.UP,
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN,
                Direction.UP,
                Direction.UP,
                Direction.LEFT,
                Direction.LEFT,
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.LEFT,
                Direction.LEFT
        );
    }

    @Override
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.growth");
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.growth");
    }

    @Override
    public String getIdentifier() {
        return "growth";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        boolean shouldReturn;

        shouldReturn = handleOffhand(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleGrowingBabies(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleFertilizingTargetBlockIfFertilizable(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleDuplicatingPlants(context);
        if (shouldReturn) {
            return;
        }

        handleConvertingBlocks(context);

    }

    private boolean handleConvertingBlocks(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock();
        BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos();

        if (targetBlockPos != null && targetBlock != null) {
            return context.blockConversion().attemptConversion(targetBlock, targetBlockPos, BLOCK_CONVERSION_DATA);
        }

        return false;
    }

    private boolean handleDuplicatingPlants(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock();

        if (targetBlock != null && isDuplicatablePlant(targetBlock)) {
            BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos();

            if (targetBlockPos == null) {
                WandWeaver.LOGGER.error("targetBlockPos during spell growth for plant duplication was null.");
            } else {
                context.entity().dropItemAt(
                        targetBlock.asItem().getDefaultStack(),
                        targetBlockPos.toCenterPos()
                );
                playBonemeal(context);
                return true;
            }
        }

        return false;
    }

    private boolean handleFertilizingTargetBlockIfFertilizable(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock();

        if (targetBlock instanceof Fertilizable fert) {
            ServerWorld world = context.player().getServerWorld();
            BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos();
            BlockState targetBlockState = context.targeting().getPlayerCrosshairTargetBlockState();
            BlockState originalBlockState = targetBlockState;

            final int limit = 100;
            boolean doesNotGrow = false;
            int count = 0;
            while (isTargetGrowable(context)) {
                if (count > 0 && originalBlockState == targetBlockState) {
                    doesNotGrow = true;
                    break;
                }

                fert.grow(
                        world,
                        world.getRandom(),
                        targetBlockPos,
                        targetBlockState
                );

                originalBlockState = targetBlockState;
                targetBlockState = world.getBlockState(targetBlockPos);

                count++;
                if (count > limit) {
                    WandWeaver.LOGGER.warn("Reached the limit in attempts to grow a block in the spell 'Growth'.");
                    WandWeaver.LOGGER.warn("Targeted block: {}", targetBlock);
                    break;
                }
            }

            if (count > 0 && (!isTargetGrowable(context) || doesNotGrow)) {
                playBonemeal(context);
                return true;
            }
        }

        return false;
    }

    private boolean handleGrowingBabies(ISpellCastingContext context) {
        Entity targetEntity = context.targeting().getPlayerCrosshairTargetEntity();

        if (targetEntity instanceof MobEntity mob && mob.isBaby()) {
            mob.setBaby(false);

            if (!mob.isBaby()) {
                mob.addVelocity(new Vec3d(0, 0.5, 0));
                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BREEZE_CHARGE);
                return true;
            }
        }

        return false;
    }

    private static final List<Item> EGG_ITEMS = List.of(Items.EGG, Items.TURTLE_EGG, Items.SNIFFER_EGG);

    private boolean handleOffhand(ISpellCastingContext context) {
        ItemStack offhand = context.player().getOffHandStack();

        if (!offhand.isEmpty()) {
            if (EGG_ITEMS.contains(offhand.getItem())) {
                Vec3d playerPos = context.player().getPos();
                ServerWorld world = context.player().getServerWorld();
                MobEntity entity;

                if (offhand.isOf(Items.EGG)) {
                    entity = new ChickenEntity(EntityType.CHICKEN, world);
                } else if (offhand.isOf(Items.TURTLE_EGG)) {
                    entity = new TurtleEntity(EntityType.TURTLE, world);
                } else {
                    entity = new SnifferEntity(EntityType.SNIFFER, world);
                }

                entity.setBaby(true);

                boolean successfulSpawn = context.entity().spawnEntity(
                        entity,
                        playerPos.x, playerPos.y + 1, playerPos.z
                );

                if (successfulSpawn) {
                    offhand.decrement(1);
                    context.sound().playSoundOnPlayer(SoundEvents.ENTITY_TURTLE_EGG_CRACK);
                    return true;
                }
            } else if (isDuplicatablePlant(offhand.getItem())) {
                offhand.increment(1);
                playBonemeal(context);
                return true;
            } else {
                return context.itemConversion().attemptConversion(ITEM_CONVERSION_DATA);
            }
        }

        return false;
    }

    private static final List<Class<?>> NON_PLANT_EXCEPTIONS = List.of(
            CactusBlock.class,
            VineBlock.class,
            SugarCaneBlock.class,
            ChorusFlowerBlock.class,
            PumpkinBlock.class,
            BambooBlock.class
    );

    private static final List<Block> NON_PLANT_EXCEPTION_BLOCKS = List.of(
            Blocks.MELON
    );

    private static final List<Block> NON_PLANT_NEGATIVE_EXCEPTION_BLOCKS = List.of(
            Blocks.DEAD_BUSH
    );

    private boolean isDuplicatablePlant(Item item) {
        if (item instanceof BlockItem blockItem) {
            return isDuplicatablePlant(blockItem.getBlock());
        }

        return false;
    }

    private boolean isDuplicatablePlant(Block block) {
        boolean isPlant = block instanceof PlantBlock;

        return (isPlant && !NON_PLANT_NEGATIVE_EXCEPTION_BLOCKS.contains(block))
                || NON_PLANT_EXCEPTIONS.stream().anyMatch(b -> b.isInstance(block))
                || NON_PLANT_EXCEPTION_BLOCKS.contains(block);
    }

    private boolean isTargetGrowable(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock();

        boolean cropAtMaxAge = false;
        if (targetBlock instanceof CropBlock crop) {
            BlockState cropState = context.targeting().getPlayerCrosshairTargetBlockState();

            cropAtMaxAge = crop.getAge(cropState) == crop.getMaxAge();
        }

        return targetBlock instanceof Fertilizable && !cropAtMaxAge; // && !blockAboveSapling;
    }

    private void playBonemeal(ISpellCastingContext context) {
        context.sound().playSoundOnPlayer(SoundEvents.ITEM_BONE_MEAL_USE);
    }
}
