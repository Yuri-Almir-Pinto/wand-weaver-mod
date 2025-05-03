package wandweaver.spells.context.utilities;

import net.minecraft.entity.LivingEntity;

public interface IEntityUtilities {
    boolean spawnEntity(LivingEntity entity, double x, double y, double z);
    boolean substituteEntity(LivingEntity target, LivingEntity transformation);
}
