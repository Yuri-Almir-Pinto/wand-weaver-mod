package wandweaver.spells.context.impl.utilities;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import wandweaver.spells.context.utilities.ISoundUtilities;

public class SoundUtilities implements ISoundUtilities {
    private final ServerPlayerEntity player;

    public SoundUtilities(ServerPlayerEntity player) {
        this.player = player;
    }

    public SoundUtilities playSoundOnPlayer(SoundEvent sound) {
        ServerWorld world = this.player.getServerWorld();

        world.playSoundFromEntity(
                null,
                this.player,
                sound,
                SoundCategory.PLAYERS,
                0.8f,
                0.8f + this.player.getRandom().nextFloat() * 0.4f
        );

        return this;
    }
}
