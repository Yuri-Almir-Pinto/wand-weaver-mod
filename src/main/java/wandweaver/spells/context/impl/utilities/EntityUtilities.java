package wandweaver.spells.context.impl.utilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import wandweaver.spells.context.utilities.IEntityUtilities;

public class EntityUtilities implements IEntityUtilities {
    private final ServerPlayerEntity player;
    private final ServerWorld world;

    public EntityUtilities(ServerPlayerEntity player) {
        this.player = player;
        this.world = player.getServerWorld();
    }
    @Override
    public boolean spawnEntity(Entity entity, double x, double y, double z) {
        boolean spawnedEntity = this.world.spawnEntity(entity);

        if (spawnedEntity) {
            entity.setPos(x, y, z);
        }

        return spawnedEntity;
    }

    @Override
    public boolean transformEntity(Entity target, Entity transformation) {
        Vec3d pos = target.getPos();

        boolean spawnedTransformation = this.spawnEntity(transformation, pos.x, pos.y, pos.z);

        if (spawnedTransformation) {
            target.discard();
        }

        return spawnedTransformation;
    }

    @Override
    public boolean dropItemAt(ItemStack stack, Vec3d pos) {
        return this.dropItemAt(stack, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean dropItemAt(ItemStack stack, double x, double y, double z) {
        ItemEntity resultEntity = new ItemEntity(
                world,
                x, y, z,
                stack
        );

        return world.spawnEntity(resultEntity);
    }
}
