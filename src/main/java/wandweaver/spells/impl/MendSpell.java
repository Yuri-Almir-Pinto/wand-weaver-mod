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
import net.minecraft.text.Text;
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
            new ItemConversionData(Items.DAMAGED_ANVIL, Items.CHIPPED_ANVIL),
            new ItemConversionData(Items.CHIPPED_ANVIL, Items.ANVIL),
            new ItemConversionData(Items.BLAZE_POWDER, Items.BLAZE_ROD, 2, 1, true),
            new ItemConversionData(Items.CRACKED_DEEPSLATE_BRICKS, Items.DEEPSLATE_BRICKS, true),
            new ItemConversionData(Items.CRACKED_DEEPSLATE_TILES, Items.DEEPSLATE_TILES, true),
            new ItemConversionData(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS, Items.POLISHED_BLACKSTONE_BRICKS, true),
            new ItemConversionData(Items.CRACKED_STONE_BRICKS, Items.STONE_BRICKS, true),
            new ItemConversionData(Items.CRACKED_NETHER_BRICKS, Items.NETHER_BRICKS, true),
            new ItemConversionData(Items.INFESTED_CRACKED_STONE_BRICKS, Items.INFESTED_STONE_BRICKS, true),
            new ItemConversionData(Items.COBBLESTONE, Items.STONE, true),
            new ItemConversionData(Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, true),
            new ItemConversionData(Items.STRIPPED_OAK_WOOD, Items.OAK_WOOD, true),
            new ItemConversionData(Items.STRIPPED_OAK_LOG, Items.OAK_LOG, true),
            new ItemConversionData(Items.STRIPPED_DARK_OAK_WOOD, Items.DARK_OAK_WOOD, true),
            new ItemConversionData(Items.STRIPPED_DARK_OAK_LOG, Items.DARK_OAK_LOG, true),
            new ItemConversionData(Items.STRIPPED_PALE_OAK_WOOD, Items.PALE_OAK_WOOD, true),
            new ItemConversionData(Items.STRIPPED_PALE_OAK_LOG, Items.PALE_OAK_LOG, true),
            new ItemConversionData(Items.STRIPPED_ACACIA_WOOD, Items.ACACIA_WOOD, true),
            new ItemConversionData(Items.STRIPPED_ACACIA_LOG, Items.ACACIA_LOG, true),
            new ItemConversionData(Items.STRIPPED_CHERRY_WOOD, Items.CHERRY_WOOD, true),
            new ItemConversionData(Items.STRIPPED_CHERRY_LOG, Items.CHERRY_LOG, true),
            new ItemConversionData(Items.STRIPPED_BIRCH_WOOD, Items.BIRCH_WOOD, true),
            new ItemConversionData(Items.STRIPPED_BIRCH_LOG, Items.BIRCH_LOG, true),
            new ItemConversionData(Items.STRIPPED_JUNGLE_WOOD, Items.JUNGLE_WOOD, true),
            new ItemConversionData(Items.STRIPPED_JUNGLE_LOG, Items.JUNGLE_LOG, true),
            new ItemConversionData(Items.STRIPPED_SPRUCE_WOOD, Items.SPRUCE_WOOD, true),
            new ItemConversionData(Items.STRIPPED_SPRUCE_LOG, Items.SPRUCE_LOG, true),
            new ItemConversionData(Items.STRIPPED_WARPED_STEM, Items.WARPED_STEM, true),
            new ItemConversionData(Items.STRIPPED_WARPED_HYPHAE, Items.WARPED_HYPHAE, true),
            new ItemConversionData(Items.STRIPPED_CRIMSON_STEM, Items.CRIMSON_STEM, true),
            new ItemConversionData(Items.STRIPPED_CRIMSON_HYPHAE, Items.CRIMSON_HYPHAE, true),
            new ItemConversionData(Items.STRIPPED_MANGROVE_WOOD, Items.MANGROVE_WOOD, true),
            new ItemConversionData(Items.STRIPPED_MANGROVE_LOG, Items.MANGROVE_LOG, true),
            new ItemConversionData(Items.STRIPPED_BAMBOO_BLOCK, Items.BAMBOO_BLOCK, true),
            new ItemConversionData(Items.BONE_MEAL, Items.BONE, 3, 1, true),
            new ItemConversionData(Items.STRING, Items.COBWEB, 5, 1, true)
    );

    private static final List<IBlockConversionData> BLOCK_CONVERSION_LIST = List.of(
            new BlockConversionData(Blocks.DAMAGED_ANVIL, Blocks.CHIPPED_ANVIL),
            new BlockConversionData(Blocks.CHIPPED_ANVIL, Blocks.ANVIL),
            new BlockConversionData(Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICKS),
            new BlockConversionData(Blocks.CRACKED_DEEPSLATE_TILES, Blocks.DEEPSLATE_TILES),
            new BlockConversionData(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS),
            new BlockConversionData(Blocks.CRACKED_STONE_BRICKS, Blocks.STONE_BRICKS),
            new BlockConversionData(Blocks.CRACKED_NETHER_BRICKS, Blocks.NETHER_BRICKS),
            new BlockConversionData(Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS),
            new BlockConversionData(Blocks.COBBLESTONE, Blocks.STONE),
            new BlockConversionData(Blocks.COBBLED_DEEPSLATE, Blocks.DEEPSLATE),
            new BlockConversionData(Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_WOOD),
            new BlockConversionData(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_LOG),
            new BlockConversionData(Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.DARK_OAK_WOOD),
            new BlockConversionData(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_LOG),
            new BlockConversionData(Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.PALE_OAK_WOOD),
            new BlockConversionData(Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_LOG),
            new BlockConversionData(Blocks.STRIPPED_ACACIA_WOOD, Blocks.ACACIA_WOOD),
            new BlockConversionData(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_LOG),
            new BlockConversionData(Blocks.STRIPPED_CHERRY_WOOD, Blocks.CHERRY_WOOD),
            new BlockConversionData(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_LOG),
            new BlockConversionData(Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_WOOD),
            new BlockConversionData(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_LOG),
            new BlockConversionData(Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_WOOD),
            new BlockConversionData(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_LOG),
            new BlockConversionData(Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_WOOD),
            new BlockConversionData(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_LOG),
            new BlockConversionData(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_STEM),
            new BlockConversionData(Blocks.STRIPPED_WARPED_HYPHAE, Blocks.WARPED_HYPHAE),
            new BlockConversionData(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_STEM),
            new BlockConversionData(Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.CRIMSON_HYPHAE),
            new BlockConversionData(Blocks.STRIPPED_MANGROVE_WOOD, Blocks.MANGROVE_WOOD),
            new BlockConversionData(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_LOG),
            new BlockConversionData(Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_BLOCK)
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
    public Text getName(@Nullable List<Direction> directions) {
        return Text.translatable("spell.name.mend");
    }

    @Override
    public Text getDescription(@Nullable List<Direction> directions) {
        return Text.translatable("spell.description.mend");
    }

    @Override
    public String getIdentifier() {
        return "mend";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        ServerPlayerEntity player = context.player();
        ItemStack offhandStack = player.getOffHandStack();

        if (!offhandStack.isEmpty()) {
            boolean successfulConversion = context.itemConversion().attemptConversion(ITEM_CONVERSION_LIST);

            if (successfulConversion) {
                this.repairSound(context);
                return;
            }

            // Attempt repairing the durability of the offhand tool.
            Integer currentDurability = offhandStack.get(DataComponentTypes.DAMAGE);
            Integer maxDurability = offhandStack.get(DataComponentTypes.MAX_DAMAGE);
            Integer priorWorkCost = offhandStack.get(DataComponentTypes.REPAIR_COST);

            if (currentDurability == null
                    || maxDurability == null
                    || priorWorkCost == null
                    || currentDurability == 0
                    || priorWorkCost >= 40) {
                return;
            }

            int repairAmount = (int) Math.ceil(maxDurability * 0.25f);

            offhandStack.set(DataComponentTypes.DAMAGE, Math.max((currentDurability - repairAmount), 0));
            offhandStack.set(DataComponentTypes.REPAIR_COST, priorWorkCost + 10);

            repairSound(context);

            return;
        }

        // Attempt healing an entity if it's a "construct".

        Entity targetEntity = context.targeting().getPlayerCrosshairTargetEntity();

        if (targetEntity instanceof GolemEntity || targetEntity instanceof SnowGolemEntity) {
            LivingEntity construct = (LivingEntity) targetEntity;
            float maxHealth = construct.getMaxHealth();
            float currentHealth = construct.getHealth();

            if (maxHealth == currentHealth) {
                return;
            }

            float repairAmount = maxHealth * 0.25f;

            construct.heal(repairAmount);

            this.repairSound(context);
            return;
        }

        // Attempt reseting the repair cost of an item.

        List<ItemEntity> itemEntities = context.targeting().getPlayerCrosshairTargetItems();

        if (!itemEntities.isEmpty()) {
            ItemStack toRepairItemStack = null;
            RepairableComponent repairableComponent = null;

            // First, attempt to grab the tool to reset the cost.
            for (ItemEntity itemEntity : itemEntities) {
                ItemStack itemStack = itemEntity.getStack();

                if (itemStack.isEmpty()) {
                    continue;
                }

                if (MendSpell.isRepairable(itemStack)) {
                    toRepairItemStack = itemStack;
                }

                if (MendSpell.hasRepairMaterial(itemStack)) {
                    repairableComponent = itemStack.get(DataComponentTypes.REPAIRABLE);
                }
            }

            // Just then attempt to find the correct material item.
            if (toRepairItemStack != null) {
                Integer repairCostTest = toRepairItemStack.get(DataComponentTypes.REPAIR_COST);

                if (repairCostTest == null || repairCostTest == 0) {
                    return;
                }

                for (ItemEntity itemEntity : itemEntities) {
                    ItemStack itemStack = itemEntity.getStack();

                    if (itemStack.isEmpty()) {
                        continue;
                    }

                    ItemStack stackToConsume;

                    if (repairableComponent == null) {
                        if (itemStack.getItem() != Items.EMERALD) {
                            continue;
                        }
                    } else {
                        RegistryEntryList<Item> materialItems = repairableComponent.items();

                        if (!materialItems.contains(itemStack.getRegistryEntry())) {
                            continue;
                        }

                    }

                    stackToConsume = itemStack;

                    stackToConsume.decrement(1);

                    Integer repairCost = toRepairItemStack.get(DataComponentTypes.REPAIR_COST);

                    if (repairCost == null) {
                        repairCost = 0;
                    }

                    toRepairItemStack.set(DataComponentTypes.REPAIR_COST, Math.max(repairCost - 40, 0));

                    this.repairSound(context);
                    return;
                }
            }
        }

        // Attempt mending a block into an unbroken state.

        Block targetBlock = context.targeting().getPlayerCrosshairTargetBlock();
        BlockPos targetBlockPos = context.targeting().getPlayerCrosshairTargetBlockPos();

        boolean successfulBlockConversion = context
                .blockConversion()
                .attemptConversion(targetBlock, targetBlockPos, BLOCK_CONVERSION_LIST);

        if (successfulBlockConversion) {
            this.repairSound(context);
        }

    }

    private static boolean isRepairable(ItemStack itemStack) {
        return itemStack.get(DataComponentTypes.MAX_DAMAGE) != null;
    }

    private static boolean hasRepairMaterial(ItemStack itemStack) {
        return itemStack.get(DataComponentTypes.REPAIRABLE) != null;
    }

    private void repairSound(ISpellCastingContext context) {
//        this.playSoundOnPlayer(context, SoundEvents.BLOCK_AMETHYST_BLOCK_STEP);
        context.sound().playSoundOnPlayer(SoundEvents.ENTITY_IRON_GOLEM_REPAIR);
    }
}

