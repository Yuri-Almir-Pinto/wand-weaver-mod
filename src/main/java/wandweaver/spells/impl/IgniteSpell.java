package wandweaver.spells.impl;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.mixin.AbstractFurnaceBlockEntityAccessor;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.data.IItemConversionData;
import wandweaver.spells.context.impl.data.ItemConversionData;
import wandweaver.utils.Direction;

import java.util.List;

public class IgniteSpell extends AbstractSpell {
    public static final List<IItemConversionData> ITEM_CONVERSION_LIST = List.of(
            new ItemConversionData(Items.STICK, Items.TORCH, true),
            new ItemConversionData(Items.POTION, Items.GLASS_BOTTLE, true),
            new ItemConversionData(Items.WATER_BUCKET, Items.BUCKET, true),
            new ItemConversionData(Items.WET_SPONGE, Items.SPONGE, true)
    );
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
            boolean successfulConversion = context.itemConversion().attemptConversion(IgniteSpell.ITEM_CONVERSION_LIST);

            if (successfulConversion) {
                context.sound().playSoundOnPlayer(SoundEvents.BLOCK_FIRE_EXTINGUISH);
                return;
            }
        }

        Entity targetEntity = context.targeting().getPlayerCrosshairTargetEntity();

        if (targetEntity != null && !(targetEntity instanceof CreeperEntity)) {
            targetEntity.setOnFireFor(4);

            context.sound().playSoundOnPlayer(SoundEvents.ENTITY_BLAZE_SHOOT);
        }

        BlockEntity targetBlockEntity = context.targeting().getPlayerCrosshairTargetBlockEntity();

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

            context.sound().playSoundOnPlayer(SoundEvents.ITEM_FLINTANDSTEEL_USE);
            return;
        }

        ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);

        boolean successfulInteraction = context.interaction().interactAsIfHolding(flintAndSteel);

        if (successfulInteraction) {
            context.sound().playSoundOnPlayer(SoundEvents.ITEM_FLINTANDSTEEL_USE);
        }
    }
}
