package wandweaver.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class Sounds {
    public static void playSoundOnEntity(Entity entity, SoundEvent sound) {
        entity.getWorld().playSoundFromEntity(
                null, entity, sound, SoundCategory.AMBIENT,
                0.8f,
                0.8f + entity.getRandom().nextFloat() * 0.4f
        );
    }

    public static void playSoundOnPlayer(PlayerEntity player, SoundEvent sound) {
        player.getWorld().playSoundFromEntity(
                null, player, sound, SoundCategory.PLAYERS,
                0.8f,
                0.8f + player.getRandom().nextFloat() * 0.4f
        );
    }
}
