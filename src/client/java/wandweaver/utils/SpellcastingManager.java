package wandweaver.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import wandweaver.WandWeaver;
import wandweaver.WandWeaverClient;
import wandweaver.configs.WandWeaverClientConfig;
import wandweaver.items.ItemsManager;
import wandweaver.network.payloads.CastSpellC2SPayload;
import wandweaver.network.payloads.CheckSpellC2SPayload;
import wandweaver.spells.ISpell;

import java.util.List;

public class SpellcastingManager {

    // --- Manual Casting State ---
    private static boolean wasManualCasting = false; // Tracks if MANUAL gesture accumulation was active in the previous tick
    private static float yawAccum = 0;     // Accumulated yaw for MANUAL casting
    private static float pitchAccum = 0;   // Accumulated pitch for MANUAL casting
    private static float prevYaw = 0;      // Player's yaw last tick (for MANUAL casting delta)
    private static float prevPitch = 0;    // Player's pitch last tick (for MANUAL casting delta)

    // --- Auto-Casting State ---
    private static boolean wasAutoCastingPreviousTick = false; // Tracks if AUTO-casting was active in the PREVIOUS tick
    private static long tickCounter = 0;           // Global tick counter for timing auto-cast gestures
    private static long autoCastBegunAtTick = -1;       // Tick when auto-casting started
    private static @Nullable ISpell autoCastStoredSpell = null;   // Spell being auto-cast
    private static List<Direction> autoCastStoredPattern = List.of(); // Gesture pattern for auto-cast

    private static boolean isSneaking = false; // Player's sneaking status, updated once per tick

    private static final float GESTURE_COMPONENT_ANGLE_ADJUSTMENT = 30.0f;
    private static final int MAX_GESTURE_COMPONENTS = 100; // Safety limit for gesture length

    /**
     * Main handler method called every client tick to manage spellcasting logic.
     */
    public static void handle(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        tickCounter++;
        isSneaking = player.isSneaking(); // Update once per tick, used by both auto and manual logic

        // --- Auto-Casting Handling ---
        // This method will manage auto-casting state and actions.
        // It also updates `wasAutoCastingPreviousTick` for the next game tick.
        processAutoCasting(player);

        // If auto-casting is currently active (i.e., key is held down),
        // skip all manual casting logic for this tick.
        if (WandWeaver.isAutoCasting) {
            // Update prevYaw/Pitch because if auto-cast stops, manual might resume,
            // or other systems might expect these to be fresh.
            // Manual gesture accumulators (yawAccum, pitchAccum) are not affected.
            prevYaw = player.getYaw();
            prevPitch = player.getPitch();
            return;
        }

        // --- Manual Casting Logic (Proceeds only if WandWeaver.isAutoCasting is false) ---
        // `wasManualCasting` here is the manual casting state flag.
        // If an auto-cast just finished, `processAutoCasting` (via `finalizeAndCastSpell`)
        // should have reset `wasManualCasting` and manual accumulators.

        // 1. Abort manual cast if wand unequipped while manual casting was intended/active
        if (isWandUnEquipped(player) && (WandWeaver.isCasting || wasManualCasting)) {
            abortCurrentManualCast();
        }

        // 2. Finalize manual cast if manual cast key released
        if (!WandWeaver.isCasting && wasManualCasting) { // `wasManualCasting` is for manual mode
            finalizeAndCastSpell(); // This clears manual gesture, sends spell, resets `wasManualCasting`
        }

        // 3. If not actively manual-casting (manual key not pressed, or cast aborted/finalized this tick)
        if (!WandWeaver.isCasting) {
            // `finalizeAndCastSpell` and `abortCurrentManualCast` already call `resetManualAccumulators`
            // and set `wasManualCasting = false`.
            // This `resetManualAccumulators()` call handles the case where `WandWeaver.isCasting` is false
            // AND `wasManualCasting` was already false (i.e., not casting and wasn't casting).
            if (!wasManualCasting) {
                resetManualAccumulators();
            }
            prevYaw = player.getYaw();
            prevPitch = player.getPitch();
            return;
        }

        // --- At this point, WandWeaver.isCasting is true (manual cast key held) ---
        // AND WandWeaver.isAutoCasting is false.

        // 4. Process ongoing manual casting gesture
        processActiveManualCastingGesture(player); // This updates `wasManualCasting` (manual)

        // 5. Store current rotation for the next tick's delta calculation
        prevYaw = player.getYaw();
        prevPitch = player.getPitch();
    }

    /**
     * Handles all logic related to auto-casting for the current tick.
     * Updates `wasAutoCastingPreviousTick`.
     */
    private static void processAutoCasting(PlayerEntity player) {
        ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
        boolean autoCastKeyCurrentlyPressed = WandWeaver.isAutoCasting;

        // --- Abort Conditions for Auto-Casting ---
        if (isWandUnEquipped(player) || !InscribedWand.isInscribed(mainHandStack)) {
            if (autoCastKeyCurrentlyPressed || wasAutoCastingPreviousTick) {
                // If auto-cast was active or intended, but now invalid (wand change/not inscripted)
                resetAutoCastState(true); // true: clear active gestures
                WandWeaver.isAutoCasting = false; // Ensure the mod knows auto-casting is off
                autoCastKeyCurrentlyPressed = false; // Reflect this change for wasAutoCastingPreviousTick update
            }
            wasAutoCastingPreviousTick = autoCastKeyCurrentlyPressed; // Update for next tick
            return;
        }

        // --- Auto-Casting State Transitions ---
        if (autoCastKeyCurrentlyPressed && !wasAutoCastingPreviousTick) {
            // Auto-cast STARTED this tick
            autoCastBegunAtTick = tickCounter;
            autoCastStoredSpell = InscribedWand.getSpell(mainHandStack);
            autoCastStoredPattern = InscribedWand.getInscription(mainHandStack);

            if (autoCastStoredSpell == null || autoCastStoredPattern == null || autoCastStoredPattern.isEmpty()) {
                // Failed to get spell/pattern from inscripted wand, abort this auto-cast attempt
                resetAutoCastState(false); // false: no "active" auto-cast gestures to clear
                WandWeaver.isAutoCasting = false; // Cancel the auto-cast input's effect
                autoCastKeyCurrentlyPressed = false; // Reflect this for wasAutoCastingPreviousTick update
            } else {
                // Successfully started: clear any previous (manual or auto) gesture data
                clearSharedGestureAndSpell();
                setAllProgressIndicators(0); // Reset manual UI indicators
                // Manual casting state (`wasManualCasting`, accumulators) is not directly touched here,
                // but `finalizeAndCastSpell` (if auto-cast completes) will reset them.
            }
        } else if (!autoCastKeyCurrentlyPressed && wasAutoCastingPreviousTick) {
            // Auto-cast STOPPED this tick (key released)
            if (autoCastStoredSpell != null) { // Check if an auto-cast sequence was actually loaded
                // Attempt to cast the spell formed by auto-gestures
                finalizeAndCastSpell(); // This uses WandWeaverClient.DIRECTION_LIST & currentSpell
                // and also resets manual `wasManualCasting` and accumulators.
            } else {
                // Auto-cast key released, but no spell was loaded (e.g., failed start due to no inscription data)
                // Ensure shared gesture state is clean anyway.
                clearSharedGestureAndSpell();
            }
            resetAutoCastState(true); // true: auto-cast sequence is ending, clear its gestures too.
        } else if (autoCastKeyCurrentlyPressed /* && wasAutoCastingPreviousTick is implied */) {
            // Auto-cast ONGOING
            if (autoCastStoredSpell != null && autoCastStoredPattern != null && !autoCastStoredPattern.isEmpty()) {
                long elapsedTicksInAutoCast = tickCounter - autoCastBegunAtTick;
                long autoDrawTimePerSegment = autoCastStoredSpell.getAutoDrawTime();
                if (autoDrawTimePerSegment <= 0) autoDrawTimePerSegment = 1; // Prevent division by zero or negative times

                int componentsAlreadyDrawn = WandWeaverClient.DIRECTION_LIST.size();
                if (componentsAlreadyDrawn < autoCastStoredPattern.size()) {
                    // Calculate total ticks required to have drawn the *next* component (at index `componentsAlreadyDrawn`)
                    long ticksRequiredForNextComponent = (long)(componentsAlreadyDrawn + 1) * autoDrawTimePerSegment;

                    if (elapsedTicksInAutoCast >= ticksRequiredForNextComponent) {
                        // It's time to add the component
                        addGestureComponentToSharedList(autoCastStoredPattern.get(componentsAlreadyDrawn), player);
                        requestSpellCheckFromServer(); // Server checks current DIRECTION_LIST
                        // WandWeaver.LOGGER.info("Auto-cast progress: " + WandWeaverClient.DIRECTION_LIST.toString());
                    }
                }
                // If pattern is complete (componentsAlreadyDrawn == autoCastStoredPattern.size()),
                // it just holds. `finalizeAndCastSpell` (called when key is released) will cast the completed spell.
            }
        }
        // Else (!autoCastKeyCurrentlyPressed && !wasAutoCastingPreviousTick): Not auto-casting, wasn't auto-casting. No action.

        wasAutoCastingPreviousTick = autoCastKeyCurrentlyPressed; // Update for the next tick
    }

    /**
     * Resets auto-cast specific state variables.
     * @param clearSharedGestures If true, also clears `WandWeaverClient.DIRECTION_LIST` and `currentSpell`.
     */
    private static void resetAutoCastState(boolean clearSharedGestures) {
        autoCastBegunAtTick = -1;
        autoCastStoredSpell = null;
        autoCastStoredPattern = List.of(); // Reset to an empty, non-null list

        if (clearSharedGestures) {
            // If an auto-cast sequence was truly active and is now ending/aborting,
            // clear the shared gesture list, recognized spell, and UI indicators.
            clearSharedGestureAndSpell();
            setAllProgressIndicators(0);
        }
    }

    /**
     * Registers the spellcasting handler with client tick events.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SpellcastingManager::handle);
    }

    /**
     * Sets the currently recognized spell. Called by network events when server confirms a spell.
     */
    public static void setCurrentSpell(ISpell spell) {
        WandWeaverClient.currentSpell = spell;
    }

    private static boolean isWandUnEquipped(PlayerEntity player) {
        return player.getStackInHand(Hand.MAIN_HAND).getItem() != ItemsManager.WAND;
    }

    /**
     * Aborts the current MANUAL casting process and resets related states.
     */
    private static void abortCurrentManualCast() {
        WandWeaver.isCasting = false; // Stop manual casting input flag
        wasManualCasting = false;     // No longer in manual gesture accumulation
        clearSharedGestureAndSpell();
        setAllProgressIndicators(0);
        resetManualAccumulators();
    }

    /**
     * Finalizes a spell cast (manual or auto).
     * Sends the recognized spell to the server if one is formed.
     * Clears shared gesture data and resets manual casting state.
     */
    private static void finalizeAndCastSpell() {
        ISpell spellToCast = WandWeaverClient.currentSpell;
        // Only cast if a spell is recognized AND there are gestures forming it.
        if (spellToCast != null && !WandWeaverClient.DIRECTION_LIST.isEmpty()) {
            ClientPlayNetworking.send(
                    new CastSpellC2SPayload(spellToCast.getIdentifier(), WandWeaverClient.DIRECTION_LIST)
            );
        }
        clearSharedGestureAndSpell();
        // Crucially, reset manual casting state, as this method can be called by auto-cast stopping.
        wasManualCasting = false;
        resetManualAccumulators();
    }

    /**
     * Handles the logic for ongoing MANUAL casting (accumulating gestures from mouse movement).
     */
    private static void processActiveManualCastingGesture(PlayerEntity player) {
        if (wasManualCasting) { // If already in a manual casting gesture sequence
            pitchAccum += player.getPitch() - prevPitch;
            yawAccum += player.getYaw() - prevYaw;
            updateManualProgressIndicatorsFromAccumulators();
        } else { // First tick of a new manual casting sequence
            clearSharedGestureAndSpell(); // Clear any prior spell/gesture data
            setAllProgressIndicators(0);  // Reset UI
            resetManualAccumulators();    // prevYaw/Pitch become 0 for initial absolute reading
        }
        wasManualCasting = true; // Mark that manual gesture accumulation is active for this tick

        detectAndRecordManualGestureComponents(player);
    }

    /**
     * Updates the UI progress indicators based on current MANUAL yaw and pitch accumulators.
     */
    private static void updateManualProgressIndicatorsFromAccumulators() {
        float angleThreshold = WandWeaverClientConfig.angleThreshold();

        if (pitchAccum != 0) {
            int progress = convertRotationToProgress(pitchAccum, angleThreshold);
            setProgressIndicator(pitchAccum < 0 ? Direction.UP : Direction.DOWN, progress);
            setProgressIndicator(pitchAccum < 0 ? Direction.DOWN : Direction.UP, 0);
        } else {
            setProgressIndicator(Direction.UP, 0);
            setProgressIndicator(Direction.DOWN, 0);
        }

        if (yawAccum != 0) {
            int progress = convertRotationToProgress(yawAccum, angleThreshold);
            setProgressIndicator(yawAccum < 0 ? Direction.LEFT : Direction.RIGHT, progress);
            setProgressIndicator(yawAccum < 0 ? Direction.RIGHT : Direction.LEFT, 0);
        } else {
            setProgressIndicator(Direction.LEFT, 0);
            setProgressIndicator(Direction.RIGHT, 0);
        }
    }

    /**
     * Checks if accumulated MANUAL rotation has crossed thresholds to form new gesture components.
     */
    private static void detectAndRecordManualGestureComponents(PlayerEntity player) {
        boolean newDirectionAdded = false;
        float angleThreshold = WandWeaverClientConfig.angleThreshold();

        if (pitchAccum <= -angleThreshold) {
            addGestureComponentToSharedList(Direction.UP, player);
            pitchAccum += GESTURE_COMPONENT_ANGLE_ADJUSTMENT;
            newDirectionAdded = true;
        }
        if (pitchAccum >= angleThreshold) {
            addGestureComponentToSharedList(Direction.DOWN, player);
            pitchAccum -= GESTURE_COMPONENT_ANGLE_ADJUSTMENT;
            newDirectionAdded = true;
        }
        if (yawAccum <= -angleThreshold) {
            addGestureComponentToSharedList(Direction.LEFT, player);
            yawAccum += GESTURE_COMPONENT_ANGLE_ADJUSTMENT;
            newDirectionAdded = true;
        }
        if (yawAccum >= angleThreshold) {
            addGestureComponentToSharedList(Direction.RIGHT, player);
            yawAccum -= GESTURE_COMPONENT_ANGLE_ADJUSTMENT;
            newDirectionAdded = true;
        }

        if (newDirectionAdded) {
            requestSpellCheckFromServer();
        }
    }

    /**
     * Resets MANUAL casting gesture accumulators and previous rotation tracking.
     */
    private static void resetManualAccumulators() {
        yawAccum = 0;
        pitchAccum = 0;
        // Setting prevYaw/Pitch to 0 makes the first delta effectively use absolute rotation,
        // matching original behavior if a cast starts from no prior movement this session.
        prevYaw = 0;
        prevPitch = 0;
    }

    /**
     * Clears the shared `WandWeaverClient.DIRECTION_LIST` and `WandWeaverClient.currentSpell`.
     */
    private static void clearSharedGestureAndSpell() {
        WandWeaverClient.DIRECTION_LIST.clear();
        setCurrentSpell(null); // Use the public setter
    }

    /**
     * Adds a detected gesture component (Direction) to the shared list, if not sneaking and under max length.
     * Plays a sound and resets manual progress indicators.
     */
    private static void addGestureComponentToSharedList(Direction direction, PlayerEntity player) {
        if (isSneaking && !WandWeaver.isAutoCasting) { // Sneaking universally prevents adding new gesture components
            return;
        }
        if (WandWeaverClient.DIRECTION_LIST.size() >= MAX_GESTURE_COMPONENTS) {
            // WandWeaver.LOGGER.warn("Max gesture components (" + MAX_GESTURE_COMPONENTS + ") reached. Not adding: " + direction);
            return;
        }

        WandWeaverClient.DIRECTION_LIST.add(direction);
        setAllProgressIndicators(0); // Reset UI for the next potential gesture segment (primarily for manual UI)

        if (player != null) {
            player.playSound(
                    SoundEvents.BLOCK_CHERRY_WOOD_BUTTON_CLICK_OFF,
                    1F,
                    1.3F + player.getWorld().getRandom().nextFloat() * 0.4F);
        }
    }

    /**
     * Sends the current gesture list to the server to check if it matches a spell.
     * This version will only send if no spell is currently recognized, matching original behavior.
     * If adding a component should *always* re-check, `WandWeaverClient.currentSpell` should be
     * set to null before calling this, or this check removed.
     */
    private static void requestSpellCheckFromServer() {
        if (WandWeaverClient.currentSpell != null) {
            // If a spell is already "locked in" for the current gesture list,
            // adding more components won't trigger a new check until the current spell is cleared
            // (e.g., by casting, or starting a new manual gesture sequence).
            return;
        }
        if (!WandWeaverClient.DIRECTION_LIST.isEmpty()) { // Don't send if list is empty
            ClientPlayNetworking.send(
                    new CheckSpellC2SPayload(WandWeaverClient.DIRECTION_LIST)
            );
        }
    }

    private static int convertRotationToProgress(float accumulatedValue, float threshold) {
        if (threshold == 0) return 100; // Avoid division by zero
        int progress = (int) (Math.abs(accumulatedValue) / threshold * 100);
        return Math.min(100, Math.max(0, progress)); // Clamp to 0-100
    }

    private static void setProgressIndicator(Direction direction, int progress) {
        int actualProgress = isSneaking ? 0 : progress; // Sneaking shows 0 progress for manual gestures
        actualProgress = Math.min(100, Math.max(0, actualProgress)); // Clamp

        switch (direction) {
            case UP -> WandWeaverClient.upProgress = actualProgress;
            case DOWN -> WandWeaverClient.downProgress = actualProgress;
            case LEFT -> WandWeaverClient.leftProgress = actualProgress;
            case RIGHT -> WandWeaverClient.rightProgress = actualProgress;
        }
    }

    private static void setAllProgressIndicators(int progress) {
        int actualProgress = isSneaking ? 0 : progress;
        actualProgress = Math.min(100, Math.max(0, actualProgress)); // Clamp

        WandWeaverClient.leftProgress = actualProgress;
        WandWeaverClient.rightProgress = actualProgress;
        WandWeaverClient.upProgress = actualProgress;
        WandWeaverClient.downProgress = actualProgress;
    }
}