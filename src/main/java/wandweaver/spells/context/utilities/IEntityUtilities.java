package wandweaver.spells.context.utilities;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public interface IEntityUtilities {
    boolean spawnEntity(Entity entity, double x, double y, double z);
    boolean transformEntity(Entity target, Entity transformation);
    boolean dropItemAt(ItemStack stack, double x, double y, double z);
    boolean dropItemAt(ItemStack stack, Vec3d pos);
}
