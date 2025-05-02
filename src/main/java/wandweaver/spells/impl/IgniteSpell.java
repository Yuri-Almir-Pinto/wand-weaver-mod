package wandweaver.spells.impl;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
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
            new ItemConversionData(Items.STICK, Items.TORCH, 1, 4, true),
            new ItemConversionData(Items.ROTTEN_FLESH, Items.LEATHER, true),
            new ItemConversionData(Items.POTION, Items.GLASS_BOTTLE, true),
            new ItemConversionData(Items.WATER_BUCKET, Items.BUCKET, true)
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
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        ServerPlayerEntity player = context.player();
        ItemStack offhandItemStack = player.getOffHandStack();

        if (!offhandItemStack.isEmpty()) {
            Item offhandItem = offhandItemStack.getItem();
            boolean successfulConversion = context.itemConversion().attemptConversion(IgniteSpell.ITEM_CONVERSION_LIST);

            if (successfulConversion) {
                if (offhandItem == Items.STICK) {
                    context.sound().playSoundOnPlayer(SoundEvents.ITEM_FLINTANDSTEEL_USE);
                }
                else {
                    context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
                }

                return;

            }

            SingleStackRecipeInput input = new SingleStackRecipeInput(offhandItemStack);
            RecipeEntry<? extends AbstractCookingRecipe> recipeEntry =
                    matchGetter.getFirstMatch(input, player.getServerWorld()).orElse(null);

            if (recipeEntry != null) {
                AbstractCookingRecipe recipe = recipeEntry.value();
                ItemStack result = recipe.craft(input, player.getServerWorld().getRegistryManager());
                player.getInventory().offerOrDrop(result);
                offhandItemStack.decrement(1);
                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
                return;
            }
        }

        List<ItemEntity> groundEntityStacks = context.targeting().getPlayerCrosshairTargetItems();

        for (ItemEntity itemEntity : groundEntityStacks) {
            ItemStack itemStack = itemEntity.getStack();

            boolean isNotConvertibleToCampfire = itemStack.isEmpty() ||
                    itemStack.streamTags().noneMatch(t -> t == ItemTags.LOGS_THAT_BURN) ||
                    itemStack.getCount() < 3;

            if (isNotConvertibleToCampfire) {
                continue;
            }

            itemStack.setCount(itemStack.getCount() - 3);

            BlockPos newCampfirePos = itemEntity.getBlockPos();

            player.getServerWorld().setBlockState(
                    newCampfirePos,
                    Blocks.CAMPFIRE.getDefaultState(),
                    Block.NOTIFY_ALL
            );

            context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);

            return;
        }

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
            return;
        }

        ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL, 1);

        boolean successfulInteraction = context.interaction().interactAsIfHolding(flintAndSteel);

        if (successfulInteraction) {
            context.sound().playSoundOnPlayer(SoundEvents.ITEM_FLINTANDSTEEL_USE);
        }
        else {
            Entity targetEntity = context.targeting().getPlayerCrosshairTargetEntity();

            if (targetEntity != null) {
                targetEntity.setOnFireFor(8);

                context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
            }
        }
    }
}
