package wandweaver.spells;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.utils.Direction;

import java.util.List;

public interface ISpell {
    List<Direction> getBasePattern();

    Text getName(@Nullable List<Direction> directions);

    Text getDescription(@Nullable List<Direction> directions);

    String getIdentifier();

    void playerCast(ServerPlayNetworking.Context context, List<Direction> pattern);


}
