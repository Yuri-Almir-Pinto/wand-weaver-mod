package wandweaver.spells;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import wandweaver.utils.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractSpell implements ISpell {
    @Override
    public abstract List<Direction> getBasePattern();

    @Override
    public abstract Text getName(@Nullable List<Direction> directions);

    @Override
    public abstract Text getDescription(@Nullable List<Direction> directions);

    @Override
    public abstract String getIdentifier();

    @Override
    public abstract void playerCast(ServerPlayNetworking.Context context, List<Direction> pattern);

    protected void playSoundOnPlayer(ServerPlayNetworking.Context context, SoundEvent sound) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();

        world.playSoundFromEntity(
                null,
                player,
                sound,
                SoundCategory.PLAYERS,
                0.8f,
                0.8f + player.getRandom().nextFloat() * 0.4f
        );
    }

    protected void convertItem(ServerPlayNetworking.Context context, ItemStack from, Item to, int fromAmount, int toAmount) {
        from.decrement(fromAmount);
        ItemStack toStack = new ItemStack(to, toAmount);
        ServerPlayerEntity player = context.player();
        player.giveOrDropStack(toStack);
    }

    protected void convertItem(ServerPlayNetworking.Context context, ItemStack from, Item to, int amount) {
        convertItem(context, from, to, amount, amount);
    }

    protected boolean interactAsIfHolding(ServerPlayNetworking.Context context, ItemStack itemStack) {
        HitResult hitResult = this.getPlayerCrosshairTarget(context);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            boolean success = this.interactBlockAsIfHolding(context, itemStack);

            if (success) {
                return success;
            }

            return this.interactItemAsIfHolding(context, itemStack);
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return this.interactEntityAsIfHolding(context, itemStack);
        }

        return false;
    }

    protected boolean interactEntityAsIfHolding(ServerPlayNetworking.Context context, ItemStack itemStack) {
        return sneakAndSelectItem(context, itemStack, () -> {
            ServerPlayerEntity player = context.player();

            EntityHitResult hitResult = (EntityHitResult) this.getPlayerCrosshairTarget(context);

            return player.interact(
                    hitResult.getEntity(),
                    Hand.MAIN_HAND
            ).isAccepted();
        });
    }

    protected boolean interactBlockAsIfHolding(ServerPlayNetworking.Context context, ItemStack itemStack) {
        return sneakAndSelectItem(context, itemStack, () -> {
            try {
                ServerPlayerEntity player = context.player();

                return player.interactionManager.interactBlock(
                        player,
                        player.getWorld(),
                        itemStack,
                        Hand.MAIN_HAND,
                        (BlockHitResult) this.getPlayerCrosshairTarget(context)
                ).isAccepted();
            } catch (ClassCastException ex) {
                // If this exception occured, it means the target is an entity, NOT a block.
                return false;
            }
        });
    }

    protected boolean interactItemAsIfHolding(ServerPlayNetworking.Context context, ItemStack itemStack) {
        return sneakAndSelectItem(context, itemStack, () -> {
            ServerPlayerEntity player = context.player();

            return player.interactionManager.interactItem(
                    player,
                    player.getWorld(),
                    itemStack,
                    Hand.MAIN_HAND
            ).isAccepted();
        });
    }

    protected @Nullable Entity getPlayerCrosshairTargetEntity(ServerPlayNetworking.Context context) {
        HitResult hitResult = this.getPlayerCrosshairTarget(context);

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) hitResult).getEntity();
        }

        return null;
    }

    protected @Nullable BlockPos getPlayerCrosshairTargetBlockPos(ServerPlayNetworking.Context context) {
        HitResult hitResult = this.getPlayerCrosshairTarget(context);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) hitResult).getBlockPos();
        }

        return null;
    }

    protected @Nullable Block getPlayerCrosshairTargetBlock(ServerPlayNetworking.Context context) {
        BlockState blockState = this.getPlayerCrosshairTargetBlockState(context);

        if (blockState != null) {
            return blockState.getBlock();
        }

        return null;
    }

    protected @Nullable BlockState getPlayerCrosshairTargetBlockState(ServerPlayNetworking.Context context) {
        BlockPos blockPos = this.getPlayerCrosshairTargetBlockPos(context);

        if (blockPos != null) {
            return context.player().getServerWorld().getBlockState(blockPos);
        }

        return null;
    }

    protected List<ItemEntity> getPlayerCrosshairTargetItems(ServerPlayNetworking.Context context) {
        ServerWorld world = context.player().getServerWorld();
        HitResult hitResult = this.getPlayerCrosshairTarget(context);

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

    protected @Nullable BlockEntity getPlayerCrosshairTargetBlockEntity(ServerPlayNetworking.Context context) {
        BlockPos blockPos = this.getPlayerCrosshairTargetBlockPos(context);

        if (blockPos != null) {
            return context.player().getServerWorld().getBlockEntity(blockPos);
        }

        return null;
    }

    protected HitResult getPlayerCrosshairTarget(ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        double playerBlockInteractionRange = player.getAttributes().getValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        double playerEntityInteractionRange = player.getAttributes().getValue(EntityAttributes.ENTITY_INTERACTION_RANGE);

        Entity camera = player.getCameraEntity();

        return findCrosshairTarget(camera, playerBlockInteractionRange, playerEntityInteractionRange, 1.0f);
    }

    private boolean sneakAndSelectItem(ServerPlayNetworking.Context context, ItemStack itemStack, Supplier<Boolean> func) {
        ServerPlayerEntity player = context.player();

        ItemStack originalItem = player.getMainHandStack();

        player.getInventory().setSelectedStack(itemStack);

        boolean originalSneaking = player.isSneaking();

        player.setSneaking(false);

        boolean result = func.get();

        player.getInventory().setSelectedStack(originalItem);

        player.setSneaking(originalSneaking);

        return result;
    }

    // Grabbed directly from the client side =w=
    private HitResult findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickProgress) {
        double d = Math.max(blockInteractionRange, entityInteractionRange);
        double e = MathHelper.square(d);
        Vec3d vec3d = camera.getCameraPosVec(tickProgress);
        HitResult hitResult = camera.raycast(d, tickProgress, false);
        double f = hitResult.getPos().squaredDistanceTo(vec3d);
        if (hitResult.getType() != HitResult.Type.MISS) {
            e = f;
            d = Math.sqrt(f);
        }

        Vec3d vec3d2 = camera.getRotationVec(tickProgress);
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

    protected record ItemConversionData(
            Item from,
            Item to,
            int cost,
            int resultAmount,
            boolean greedyConversion
    ) {
        public ItemConversionData(Item from, Item to) {
            this(from, to, 1, 1, false);
        }

        public ItemConversionData(Item from, Item to, boolean greedyConversion) {
            this(from, to, 1, 1, greedyConversion);
        }

        public ItemConversionData(Item from, Item to, int cost, int resultAmount, boolean greedyConversion) {
            this.from = from;
            this.to = to;
            this.cost = cost;
            this.resultAmount = resultAmount;
            this.greedyConversion = greedyConversion;
        }

        public boolean canConvert(ServerPlayNetworking.Context context) {
            ItemStack offhand = context.player().getOffHandStack();

            return !offhand.isEmpty()
                    && offhand.getCount() >= cost
                    && offhand.getItem() == from;
        }

        public void convert(ServerPlayNetworking.Context context) {
            ItemStack offhand = context.player().getOffHandStack();
            PlayerEntity player = context.player();

            if (!canConvert(context)) {
                return;
            }

            int offhandAmount = offhand.getCount();

            if (!greedyConversion) {
                List<ItemStack> itemStacks = getItemStacks(to, resultAmount);

                for (int i = 0; i < itemStacks.size(); i++) {
                    if (i == 0 && offhandAmount == cost) {
                        player.setStackInHand(Hand.OFF_HAND, itemStacks.get(i));
                    }
                    else {
                        ItemStack stack = itemStacks.get(i);

                        player.giveOrDropStack(stack);
                    }
                }

            }
            else {
                int conversionAmount = (int) Math.floor((double) offhandAmount / cost);

                int resultAmount = conversionAmount * this.resultAmount;
                int costAmount = conversionAmount * this.cost;

                offhand.decrement(costAmount);
                List<ItemStack> results = getItemStacks(to, resultAmount);

                for (int i = 0; i < results.size(); i++) {
                    if (i == 0 && offhand.isEmpty()) {
                        player.setStackInHand(Hand.OFF_HAND, results.get(i));
                    }
                    else {
                        ItemStack stack = results.get(i);

                        player.giveOrDropStack(stack);
                    }
                }
            }
        }

        private static List<ItemStack> getItemStacks(Item to, int resultAmount) {
            ArrayList<ItemStack> itemStacks = new ArrayList<>();

            if (resultAmount <= 64) {
                itemStacks.add(new ItemStack(to, resultAmount));
            }
            else {
                int amount = resultAmount;
                while (amount > 0) {
                    int stackAmount = Math.min(amount, 64);
                    itemStacks.add(new ItemStack(to, stackAmount));
                    amount -= stackAmount;
                }
            }

            return itemStacks;
        }
    }

    protected record BlockConversionData(
            Block from,
            Block to
    ) {
        public BlockConversionData(Block from, Block to) {
            this.from = from;
            this.to = to;
        }

        public boolean canConvert(ServerPlayNetworking.Context context, Block target) {
            return target != null && target == from;
        }

        public void convert(ServerPlayNetworking.Context context, Block target, BlockPos targetPos) {
            if (canConvert(context, target)) {
                ServerWorld world = context.player().getServerWorld();
                world.setBlockState(targetPos, to.getDefaultState());
            }
        }
    }
}
