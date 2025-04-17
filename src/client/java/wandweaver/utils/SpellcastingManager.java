package wandweaver.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import wandweaver.WandWeaver;
import wandweaver.WandWeaverClient;
import wandweaver.configs.WandWeaverConfig;
import wandweaver.network.payloads.CastSpellC2SPayload;
import wandweaver.network.payloads.CheckSpellC2SPayload;
import wandweaver.spells.ISpell;

public class SpellcastingManager {
    private static MinecraftClient minecraftClient;
    private static boolean wasCasting = false;
    private static boolean isSneaking = false;
    private static float yawAccum = 0;
    private static float pitchAccum = 0;
    private static float prevYaw = 0;
    private static float prevPitch = 0;

    public static void handle(MinecraftClient client) {
        minecraftClient = client;
        PlayerEntity player = minecraftClient.player;

        if (player == null) {
            return;
        }

        if (player.getStackInHand(Hand.MAIN_HAND).getItem() != Items.STICK && (getIsCasting() || wasCasting)) {
            reset();
            clear();
            setAllProgress(0);
        }

        isSneaking = player.isSneaking();

        if (stoppedCasting()) {
            castCurrentSpell();
        }

        if (!getIsCasting()) {
            SpellcastingManager.reset();
            return;
        }

        if (wasCasting) {
            pitchAccum += player.getPitch() - prevPitch;
            yawAccum += player.getYaw() - prevYaw;

            if (pitchAccum < 0) {
                setProgress(Direction.UP, toProgress(pitchAccum));
                setProgress(Direction.DOWN, 0);
            }
            if (pitchAccum > 0) {
                setProgress(Direction.DOWN, toProgress(pitchAccum));
                setProgress(Direction.UP, 0);
            }
            if (yawAccum > 0) {
                setProgress(Direction.RIGHT, toProgress(yawAccum));
                setProgress(Direction.LEFT, 0);
            }
            if (yawAccum < 0) {
                setProgress(Direction.LEFT, toProgress(yawAccum));
                setProgress(Direction.RIGHT, 0);
            }

            if (pitchAccum == 0) {
                setProgress(Direction.UP, toProgress(pitchAccum));
                setProgress(Direction.DOWN, toProgress(pitchAccum));
            }
            if (yawAccum == 0) {
                setProgress(Direction.LEFT, toProgress(yawAccum));
                setProgress(Direction.RIGHT, toProgress(yawAccum));
            }
        }
        else {
            setAllProgress(0);
        }

        wasCasting(true);

        boolean newDirection = false;

        if (pitchAccum >= WandWeaverConfig.angleThreshold()) {
            add(Direction.DOWN);
            pitchAccum -= 30;
            newDirection = true;
        }
        if (pitchAccum <= -WandWeaverConfig.angleThreshold()) {
            add(Direction.UP);
            pitchAccum += 30;
            newDirection = true;
        }
        if (yawAccum >= WandWeaverConfig.angleThreshold()) {
            add(Direction.RIGHT);
            yawAccum -= 30;
            newDirection = true;
        }
        if (yawAccum <= -WandWeaverConfig.angleThreshold()) {
            add(Direction.LEFT);
            yawAccum += 30;
            newDirection = true;
        }

        if (newDirection) {
            checkSpell();
        }

        prevYaw = player.getYaw();
        prevPitch = player.getPitch();
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SpellcastingManager::handle);
    }

    private static void reset() {
        WandWeaver.isCasting = false;
        wasCasting = false;
        resetAccum();
    }

    private static void resetAccum() {
        yawAccum = 0;
        pitchAccum = 0;
        prevYaw = 0;
        prevPitch = 0;
    }

    private static boolean getIsCasting() {
        return WandWeaver.isCasting;
    }

    private static void wasCasting(boolean value) {
        wasCasting = value;
    }

    private static boolean stoppedCasting() {
        return !WandWeaver.isCasting && wasCasting;
    }

    private static void clear() {
        WandWeaverClient.DIRECTION_LIST.clear();
        setCurrentSpell(null);
    }

    private static boolean directionListIsEmpty() {
        return WandWeaverClient.DIRECTION_LIST.isEmpty();
    }

    private static void add(Direction direction) {
        if (isSneaking) {
            return;
        }

        WandWeaverClient.DIRECTION_LIST.add(direction);
        setAllProgress(0);

        ClientPlayerEntity player = minecraftClient.player;

        if (player == null) {
            return;
        }

        player.playSound(
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                0.3F,
                0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void checkSpell() {
        if (getCurrentSpell() != null) {
            return;
        }

        ClientPlayNetworking.send(
                new CheckSpellC2SPayload(WandWeaverClient.DIRECTION_LIST)
        );
    }

    private static void castCurrentSpell() {
        ISpell currentSpell = getCurrentSpell();

        if (currentSpell != null) {
            ClientPlayNetworking.send(
                    new CastSpellC2SPayload(currentSpell.getIdentifier(), WandWeaverClient.DIRECTION_LIST)
            );
        }

        clear();
    }

    public static void setCurrentSpell(ISpell spell) {
        WandWeaverClient.currentSpell = spell;
    }

    private static ISpell getCurrentSpell() {
        return WandWeaverClient.currentSpell;
    }

    public static int toProgress(float value) {
        return (int) (Math.abs(value) / WandWeaverConfig.angleThreshold() * 100);
    }

    public static void setProgress(Direction direction, int progress) {
        if (isSneaking) {
            progress = 0;
        }

        switch (direction) {
            case UP -> WandWeaverClient.upProgress = progress;
            case DOWN -> WandWeaverClient.downProgress = progress;
            case LEFT -> WandWeaverClient.leftProgress = progress;
            case RIGHT -> WandWeaverClient.rightProgress = progress;
        }
    }

    public static void setAllProgress(int progress) {
        WandWeaverClient.leftProgress = progress;
        WandWeaverClient.rightProgress = progress;
        WandWeaverClient.upProgress = progress;
        WandWeaverClient.downProgress = progress;
    }
}
