package wandweaver.spells.context.impl.utilities;

import net.minecraft.entity.LivingEntity;
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
    public boolean spawnEntity(LivingEntity entity, double x, double y, double z) {
        boolean spawnedEntity = this.world.spawnEntity(entity);

        if (spawnedEntity) {
            entity.setPos(x, y, z);
        }

        return spawnedEntity;
    }

    @Override
    public boolean substituteEntity(LivingEntity target, LivingEntity transformation) {
        Vec3d pos = target.getPos();

        boolean spawnedTransformation = this.spawnEntity(transformation, pos.x, pos.y, pos.z);

        if (spawnedTransformation) {
            target.discard();
        }

        return spawnedTransformation;
    }
}
