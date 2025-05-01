package wandweaver.spells.context.impl.utilities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.utilities.ITargetingUtilities;

import java.util.List;

public class TargetingUtilities implements ITargetingUtilities {
    private final ServerPlayerEntity player;

    public TargetingUtilities(ServerPlayerEntity player) {
        this.player = player;
    }

    public @Nullable Entity getPlayerCrosshairTargetEntity() {
        HitResult hitResult = this.getPlayerCrosshairTarget();

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) hitResult).getEntity();
        }

        return null;
    }

    public @Nullable BlockPos getPlayerCrosshairTargetBlockPos() {
        HitResult hitResult = this.getPlayerCrosshairTarget();

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) hitResult).getBlockPos();
        }

        return null;
    }

    public @Nullable Block getPlayerCrosshairTargetBlock() {
        BlockState blockState = this.getPlayerCrosshairTargetBlockState();

        if (blockState != null) {
            return blockState.getBlock();
        }

        return null;
    }

    public @Nullable BlockState getPlayerCrosshairTargetBlockState() {
        BlockPos blockPos = this.getPlayerCrosshairTargetBlockPos();

        if (blockPos != null) {
            return this.player.getServerWorld().getBlockState(blockPos);
        }

        return null;
    }

    public List<ItemEntity> getPlayerCrosshairTargetItems() {
        ServerWorld world = this.player.getServerWorld();
        HitResult hitResult = this.getPlayerCrosshairTarget();

        switch (hitResult) {
            case BlockHitResult blockHitResult -> {
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockPos itemsLocation = blockPos.offset(blockHitResult.getSide());

                return world.getOtherEntities(null, new Box(itemsLocation).expand(0.5, 0.5, 0.5))
                        .stream()
                        .filter(e -> e instanceof ItemEntity)
                        .map(e -> (ItemEntity) e)
                        .toList();
            }
            case EntityHitResult entityHitResult -> {
                Entity entity = entityHitResult.getEntity();

                return world.getOtherEntities(null, entity.getBoundingBox().expand(0.5, 0.5, 0.5))
                        .stream()
                        .filter(e -> e instanceof ItemEntity)
                        .map(e -> (ItemEntity) e)
                        .toList();
            }
            default -> {
                return List.of();
            }
        }
    }

    public @Nullable BlockEntity getPlayerCrosshairTargetBlockEntity() {
        BlockPos blockPos = this.getPlayerCrosshairTargetBlockPos();

        if (blockPos != null) {
            return this.player.getServerWorld().getBlockEntity(blockPos);
        }

        return null;
    }

    public HitResult getPlayerCrosshairTarget() {
        double playerBlockInteractionRange = this.player.getAttributes().getValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        double playerEntityInteractionRange = this.player.getAttributes().getValue(EntityAttributes.ENTITY_INTERACTION_RANGE);

        Entity camera = player.getCameraEntity();

        return findCrosshairTarget(camera, playerBlockInteractionRange, playerEntityInteractionRange);
    }

    // Grabbed directly from the client side =w=
    private HitResult findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange) {
        double d = Math.max(blockInteractionRange, entityInteractionRange);
        double e = MathHelper.square(d);
        Vec3d vec3d = camera.getCameraPosVec((float) 1.0);
        HitResult hitResult = camera.raycast(d, (float) 1.0, false);
        double f = hitResult.getPos().squaredDistanceTo(vec3d);
        if (hitResult.getType() != HitResult.Type.MISS) {
            e = f;
            d = Math.sqrt(f);
        }

        Vec3d vec3d2 = camera.getRotationVec((float) 1.0);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        Box box = camera.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(camera, vec3d, vec3d3, box, EntityPredicates.CAN_HIT, e);
        return entityHitResult != null && entityHitResult.getPos().squaredDistanceTo(vec3d) < f
                ? ensureTargetInRange(entityHitResult, vec3d, entityInteractionRange)
                : ensureTargetInRange(hitResult, vec3d, blockInteractionRange);
    }

    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        Vec3d vec3d = hitResult.getPos();
        if (!vec3d.isInRange(cameraPos, interactionRange)) {
            Vec3d vec3d2 = hitResult.getPos();
            net.minecraft.util.math.Direction direction = net.minecraft.util.math.Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
            return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
        } else {
            return hitResult;
        }
    }
}
