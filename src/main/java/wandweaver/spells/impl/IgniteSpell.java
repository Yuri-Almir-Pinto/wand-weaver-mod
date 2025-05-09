package wandweaver.spells.impl;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import wandweaver.mixin.AbstractFurnaceBlockEntityAccessor;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.data.IItemConversionData;
import wandweaver.spells.context.impl.data.ItemConversionData;
import wandweaver.utils.Direction;

import java.util.List;

public class IgniteSpell extends AbstractSpell {
    private static final List<IItemConversionData> ITEM_CONVERSION_LIST = List.of(
            ItemConversionData.Builder.begin(Items.STICK, Items.TORCH)
                    .cost(1).result(4).greedy(true).sound(SoundEvents.ITEM_FLINTANDSTEEL_USE).build(),
            ItemConversionData.Builder.begin(Items.ROTTEN_FLESH, Items.LEATHER)
                    .greedy(true).sound(SoundEvents.BLOCK_FIRE_EXTINGUISH).build(),
            ItemConversionData.Builder.begin(Items.POTION, Items.GLASS_BOTTLE)
                    .greedy(true).sound(SoundEvents.BLOCK_FIRE_EXTINGUISH).build(),
            ItemConversionData.Builder.begin(Items.WATER_BUCKET, Items.BUCKET)
                    .greedy(true).sound(SoundEvents.BLOCK_FIRE_EXTINGUISH).build()
    );

    private static final ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter =
            ServerRecipeManager.createCachedMatchGetter(RecipeType.SMELTING);

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
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.ignite");
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.ignite");
    }

    @Override
    public String getIdentifier() {
        return "ignite";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        boolean shouldReturn;

        shouldReturn = handleOffhand(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleVaporizingWater(context);
        if (shouldReturn) {
            return;
        }

        if (!context.player().isSneaking()) {
            shouldReturn = handleFurnaceAsTarget(context);
            if (shouldReturn) {
                return;
            }

            shouldReturn = handleItemsOnTheGround(context);
            if (shouldReturn) {
                return;
            }

            shouldReturn = handleCookingTargetBlock(context);
            if (shouldReturn) {
                return;
            }
        }

        shouldReturn = handleSettingEntitiesOnFire(context);
        if (shouldReturn) {
            return;
        }

        handleUsingFlintAndSteel(context);
    }

    private boolean handleVaporizingWater(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock(true);

        if (targetBlock == Blocks.WATER) {
            BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos(true);

            context.player()
                    .getServerWorld()
                    .setBlockState(targetBlockPos, Blocks.AIR.getDefaultState());

            context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
            return true;
        }

        return false;
    }

    private boolean handleUsingFlintAndSteel(ISpellCastingContext context) {
        ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL, 1);

        boolean successfulInteraction = context.interaction().interactAsIfHolding(flintAndSteel);

        if (successfulInteraction) {
            context.sound().playSoundOnPlayer(SoundEvents.ITEM_FLINTANDSTEEL_USE);
        }

        return successfulInteraction;
    }

    private boolean handleSettingEntitiesOnFire(ISpellCastingContext context) {
        Entity targetEntity = context.targeting().getPlayerCrosshairTargetEntity();

        if (targetEntity != null && !(targetEntity instanceof CreeperEntity)) {
            targetEntity.setOnFireFor(8);

            context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
            return true;
        }

        return false;
    }

    private boolean handleCookingTargetBlock(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock();
        BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos();

        if (targetBlock != null && targetBlockPos != null && canCook(context, targetBlock.asItem().getDefaultStack())) {
            ServerWorld world = context.player().getServerWorld();
            ItemStack result = cook(context, targetBlock.asItem().getDefaultStack());

            if (result != null && result.getItem() instanceof BlockItem blockItem) {
                world.setBlockState(targetBlockPos, blockItem.getBlock().getDefaultState());
                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
                return true;
            } else if (result != null) {
                world.removeBlock(targetBlockPos, false);

                context.entity().dropItemAt(result, targetBlockPos.toCenterPos());

                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
                return true;
            }
        }

        return false;
    }

    private boolean handleFurnaceAsTarget(ISpellCastingContext context) {
        ServerPlayerEntity player = context.player();
        BlockEntity targetBlockEntity = context.targeting().getPlayerCrosshairTargetBlockEntity();

        if (targetBlockEntity instanceof AbstractFurnaceBlockEntity furnaceBlock) {
            AbstractFurnaceBlockEntityAccessor furnaceAccessor = (AbstractFurnaceBlockEntityAccessor) furnaceBlock;
            // For some reason, the burn ticks are offset by 1. 200/400 does not complete a recipe.
            int amountOfItems = 4;
            int extraTime = ((furnaceBlock instanceof SmokerBlockEntity || furnaceBlock instanceof BlastFurnaceBlockEntity
                    ? 100 : 200) * amountOfItems) + 1;

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

            context.sound().playSoundOnPlayer(SoundEvents.ITEM_FLINTANDSTEEL_USE);
            return true;
        }

        return false;
    }

    private boolean handleOffhand(ISpellCastingContext context) {
        ServerPlayerEntity player = context.player();
        ItemStack offhandItemStack = player.getOffHandStack();

        if (!offhandItemStack.isEmpty()) {
            boolean successfulConversion = context.itemConversion().attemptConversion(IgniteSpell.ITEM_CONVERSION_LIST);

            if (successfulConversion) {
                return true;
            }

            if (canCook(context, offhandItemStack)) {
                ItemStack result = cook(context, offhandItemStack);
                offhandItemStack.decrement(1);

                if (offhandItemStack.isEmpty()) {
                    player.setStackInHand(Hand.OFF_HAND, result);
                } else {
                    player.getInventory().offerOrDrop(result);
                }

                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
                return true;
            }
        }

        return false;
    }

    private boolean handleItemsOnTheGround(ISpellCastingContext context) {
        ServerPlayerEntity player = context.player();
        List<ItemEntity> groundEntityStacks = context.targeting().getPlayerCrosshairTargetItems();

        for (ItemEntity itemEntity : groundEntityStacks) {
            ItemStack itemStack = itemEntity.getStack();

            if (isConvertibleToCampfire(itemStack)) {
                itemStack.setCount(itemStack.getCount() - 3);

                BlockPos newCampfirePos = itemEntity.getBlockPos();

                player.getServerWorld().setBlockState(
                        newCampfirePos,
                        Blocks.CAMPFIRE.getDefaultState(),
                        Block.NOTIFY_ALL
                );

                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
                return true;
            } else if (canCook(context, itemStack)) {
                ItemStack result = cook(context, itemStack);

                if (result.isEmpty()) {
                    return false;
                }

                context.entity().dropItemAt(result, itemEntity.getPos());
                itemStack.decrement(1);
                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
                return true;
            }
        }

        return false;
    }

    private boolean isConvertibleToCampfire(ItemStack stack) {
        return !stack.isEmpty() &&
                stack.streamTags().anyMatch(t -> t == ItemTags.LOGS_THAT_BURN) &&
                stack.getCount() >= 3;
    }

    private boolean canCook(ISpellCastingContext context, ItemStack stack) {
        SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
        RecipeEntry<? extends AbstractCookingRecipe> recipeEntry =
                matchGetter.getFirstMatch(input, context.player().getServerWorld()).orElse(null);

        return recipeEntry != null;
    }

    private ItemStack cook(ISpellCastingContext context, ItemStack stack) {
        ServerPlayerEntity player = context.player();

        SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
        RecipeEntry<? extends AbstractCookingRecipe> recipeEntry =
                matchGetter.getFirstMatch(input, player.getServerWorld()).orElse(null);

        if (recipeEntry == null) {
            return ItemStack.EMPTY;
        }

        AbstractCookingRecipe recipe = recipeEntry.value();
        return recipe.craft(input, player.getServerWorld().getRegistryManager());
    }
}
