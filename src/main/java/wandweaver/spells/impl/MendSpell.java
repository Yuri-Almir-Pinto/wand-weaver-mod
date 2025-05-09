package wandweaver.spells.impl;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
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

public class MendSpell extends AbstractSpell {
    private static final List<IItemConversionData> ITEM_CONVERSION_LIST = List.of(
            ItemConversionData.Builder.begin(Items.DAMAGED_ANVIL, Items.CHIPPED_ANVIL).build(),
            ItemConversionData.Builder.begin(Items.CHIPPED_ANVIL, Items.ANVIL).build(),
            ItemConversionData.Builder.begin(Items.BLAZE_POWDER, Items.BLAZE_ROD)
                    .cost(2).result(1).greedy(true).build(),
            ItemConversionData.Builder.begin(Items.CRACKED_DEEPSLATE_BRICKS, Items.DEEPSLATE_BRICKS)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.CRACKED_DEEPSLATE_TILES, Items.DEEPSLATE_TILES)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS, Items.POLISHED_BLACKSTONE_BRICKS)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.CRACKED_STONE_BRICKS, Items.STONE_BRICKS)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.CRACKED_NETHER_BRICKS, Items.NETHER_BRICKS)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.INFESTED_CRACKED_STONE_BRICKS, Items.INFESTED_STONE_BRICKS)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.COBBLESTONE, Items.STONE)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.COBBLED_DEEPSLATE, Items.DEEPSLATE)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_OAK_WOOD, Items.OAK_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_OAK_LOG, Items.OAK_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_DARK_OAK_WOOD, Items.DARK_OAK_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_DARK_OAK_LOG, Items.DARK_OAK_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_PALE_OAK_WOOD, Items.PALE_OAK_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_PALE_OAK_LOG, Items.PALE_OAK_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_ACACIA_WOOD, Items.ACACIA_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_ACACIA_LOG, Items.ACACIA_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_CHERRY_WOOD, Items.CHERRY_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_CHERRY_LOG, Items.CHERRY_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_BIRCH_WOOD, Items.BIRCH_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_BIRCH_LOG, Items.BIRCH_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_JUNGLE_WOOD, Items.JUNGLE_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_JUNGLE_LOG, Items.JUNGLE_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_SPRUCE_WOOD, Items.SPRUCE_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_SPRUCE_LOG, Items.SPRUCE_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_WARPED_STEM, Items.WARPED_STEM)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_WARPED_HYPHAE, Items.WARPED_HYPHAE)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_CRIMSON_STEM, Items.CRIMSON_STEM)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_CRIMSON_HYPHAE, Items.CRIMSON_HYPHAE)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_MANGROVE_WOOD, Items.MANGROVE_WOOD)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_MANGROVE_LOG, Items.MANGROVE_LOG)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRIPPED_BAMBOO_BLOCK, Items.BAMBOO_BLOCK)
                    .greedy(true).build(),
            ItemConversionData.Builder.begin(Items.BONE_MEAL, Items.BONE)
                    .cost(3).result(1).greedy(true).build(),
            ItemConversionData.Builder.begin(Items.STRING, Items.COBWEB)
                    .cost(1).result(1).greedy(true).build()
    );

    private static final List<IBlockConversionData> BLOCK_CONVERSION_LIST = List.of(
            BlockConversionData.Builder.begin(Blocks.DAMAGED_ANVIL, Blocks.CHIPPED_ANVIL).build(),
            BlockConversionData.Builder.begin(Blocks.CHIPPED_ANVIL, Blocks.ANVIL).build(),
            BlockConversionData.Builder.begin(Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICKS).build(),
            BlockConversionData.Builder.begin(Blocks.CRACKED_DEEPSLATE_TILES, Blocks.DEEPSLATE_TILES).build(),
            BlockConversionData.Builder.begin(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS).build(),
            BlockConversionData.Builder.begin(Blocks.CRACKED_STONE_BRICKS, Blocks.STONE_BRICKS).build(),
            BlockConversionData.Builder.begin(Blocks.CRACKED_NETHER_BRICKS, Blocks.NETHER_BRICKS).build(),
            BlockConversionData.Builder.begin(Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS).build(),
            BlockConversionData.Builder.begin(Blocks.COBBLESTONE, Blocks.STONE).build(),
            BlockConversionData.Builder.begin(Blocks.COBBLED_DEEPSLATE, Blocks.DEEPSLATE).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.DARK_OAK_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.PALE_OAK_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_ACACIA_WOOD, Blocks.ACACIA_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_CHERRY_WOOD, Blocks.CHERRY_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_STEM).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_WARPED_HYPHAE, Blocks.WARPED_HYPHAE).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_STEM).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.CRIMSON_HYPHAE).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_MANGROVE_WOOD, Blocks.MANGROVE_WOOD).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_LOG).build(),
            BlockConversionData.Builder.begin(Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_BLOCK).build()
    );


    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN,
                Direction.LEFT,
                Direction.LEFT,
                Direction.LEFT
        );
    }

    @Override
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.mend");
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.mend");
    }

    @Override
    public String getIdentifier() {
        return "mend";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        boolean shouldReturn;

        shouldReturn = handleOffhand(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleRepairingConstruct(context);
        if (shouldReturn) {
            return;
        }

        shouldReturn = handleResettingRepairCostFromGroundItems(context);
        if (shouldReturn) {
            return;
        }

        handleConvertingTargetBlock(context);
    }

    private boolean handleConvertingTargetBlock(ISpellCastingContext context) {
        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock();
        BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos();

        boolean successfulBlockConversion = context
                .blockConversion()
                .attemptConversion(targetBlock, targetBlockPos, BLOCK_CONVERSION_LIST);

        if (successfulBlockConversion) {
            this.repairSound(context);
        }

        return successfulBlockConversion;
    }

    private boolean handleResettingRepairCostFromGroundItems(ISpellCastingContext context) {
        List<ItemEntity> itemEntities = context.targeting().getPlayerCrosshairTargetItems();

        if (itemEntities.isEmpty()) {
            return false;
        }

        ItemEntity toolEntity = itemEntities
                .stream()
                .filter(e -> hasRepairCost(e.getStack()))
                .findFirst().orElse(null);

        ItemStack tool = toolEntity == null ? ItemStack.EMPTY : toolEntity.getStack();

        if (tool.isEmpty()) {
            return false;
        }

        RegistryEntryList<Item> repairMaterials = getRepairMaterials(tool);

        if (isGoldTool(tool)) {
            restoreGoldTool(tool);
            repairSound(context);
            return true;
        }

        ItemEntity materialEntity = itemEntities.stream()
                .filter(e -> repairMaterials.contains(e.getStack().getRegistryEntry())
                        || e.getStack().getItem() == Items.GOLD_INGOT)
                .findFirst().orElse(null);

        ItemStack material = materialEntity == null ? ItemStack.EMPTY : materialEntity.getStack();

        if (material.isEmpty()) {
            return false;
        }

        boolean reducedCost;

        if (material.isOf(Items.GOLD_INGOT)) {
            reducedCost = reduceRepairCost(tool, 5);
        } else {
            reducedCost = reduceRepairCost(tool, 40);
        }

        if (reducedCost) {
            material.decrement(1);
            repairSound(context);
            return true;
        }

        return false;
    }

    private boolean handleRepairingConstruct(ISpellCastingContext context) {
        Entity targetEntity = context.targeting().getPlayerCrosshairTargetEntity();

        if (targetEntity instanceof GolemEntity || targetEntity instanceof SnowGolemEntity) {
            LivingEntity construct = (LivingEntity) targetEntity;
            float maxHealth = construct.getMaxHealth();
            float currentHealth = construct.getHealth();

            if (maxHealth == currentHealth) {
                return false;
            }

            float repairAmount = maxHealth * 0.25f;

            construct.heal(repairAmount);

            this.repairSound(context);
            return true;
        }

        return false;
    }

    private boolean handleOffhand(ISpellCastingContext context) {
        ServerPlayerEntity player = context.player();
        ItemStack offhand = player.getOffHandStack();

        if (!offhand.isEmpty()) {
            boolean successfulConversion = context.itemConversion().attemptConversion(ITEM_CONVERSION_LIST);

            if (successfulConversion) {
                this.repairSound(context);
                return true;
            }


            Integer currentDurability = offhand.get(DataComponentTypes.DAMAGE);
            Integer maxDurability = offhand.get(DataComponentTypes.MAX_DAMAGE);
            Integer priorWorkCost = offhand.get(DataComponentTypes.REPAIR_COST);

            if (isGoldTool(offhand)) {
                restoreGoldTool(offhand);
                repairSound(context);
                return true;
            }

            if (currentDurability == null
                    || maxDurability == null
                    || priorWorkCost == null
                    || currentDurability == 0
                    || priorWorkCost >= 40) {
                return false;
            }

            int repairAmount = (int) Math.ceil(maxDurability * 0.25f);

            offhand.set(DataComponentTypes.DAMAGE, Math.max((currentDurability - repairAmount), 0));
            offhand.set(DataComponentTypes.REPAIR_COST, priorWorkCost + 10);

            repairSound(context);

            return true;
        }

        return false;
    }

    private boolean reduceRepairCost(ItemStack stack, int amount) {
        Integer repairCost = stack.get(DataComponentTypes.REPAIR_COST);

        if (repairCost == null) {
            repairCost = 0;
        }

        int newCost = Math.max(repairCost - amount, 0);

        if (newCost == repairCost) {
            return false;
        }

        stack.set(DataComponentTypes.REPAIR_COST, newCost);

        return true;
    }

    private void restoreGoldTool(ItemStack stack) {
        Integer maxDurability = stack.get(DataComponentTypes.MAX_DAMAGE);

        if (maxDurability == null) {
            throw new IllegalStateException("Max durability of a gold tool was null when attempting to cast 'mend' on it.");
        }

        stack.set(DataComponentTypes.DAMAGE, 0);
        stack.set(DataComponentTypes.MAX_DAMAGE, (int) (maxDurability * 1.2));
        stack.set(DataComponentTypes.REPAIR_COST, 0);

        if (maxDurability == Integer.MAX_VALUE) {
            stack.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
        }
    }

    private boolean isGoldTool(ItemStack itemStack) {
        RepairableComponent component = itemStack.get(DataComponentTypes.REPAIRABLE);

        if (component == null) {
            return false;
        }

        return component.items().size() == 1
                && component.items().contains(Items.GOLD_INGOT.getDefaultStack().getRegistryEntry());
    }

    private RegistryEntryList<Item> getRepairMaterials(ItemStack stack) {
        RepairableComponent component = stack.get(DataComponentTypes.REPAIRABLE);

        if (component == null) {
            return RegistryEntryList.empty();
        }

        return component.items();
    }

    private boolean hasRepairCost(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0) != 0;
    }

    private void repairSound(ISpellCastingContext context) {
        context.sound().playSoundOnPlayer(SoundEvents.ENTITY_IRON_GOLEM_REPAIR);
    }
}

