package wandweaver.spells.context.utilities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ITargetingUtilities {
    @Nullable Entity getPlayerCrosshairTargetEntity();
    @Nullable BlockPos getPlayerCrosshairTargetBlockPos();
    @Nullable Block getPlayerCrosshairTargetBlock();
    @Nullable BlockState getPlayerCrosshairTargetBlockState();
    @Nullable BlockEntity getPlayerCrosshairTargetBlockEntity();
    List<ItemEntity> getPlayerCrosshairTargetItems();
    HitResult getPlayerCrosshairTarget();
}
