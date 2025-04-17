package wandweaver.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wandweaver.WandWeaver;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    // Avoid logging when the player is trying to select a special item slot, and also actually sync the inventory
    // when the client calls to change the selected slot.
    @Inject(at = @At("HEAD"), method = "onUpdateSelectedSlot", cancellable = true)
    private void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        if (WandWeaver.SPECIAL_ITEM_SLOTS.contains(packet.getSelectedSlot())) {
            NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object)this, this.player.getServerWorld());
            if (this.player.getInventory().getSelectedSlot() != packet.getSelectedSlot() && this.player.getActiveHand() == Hand.MAIN_HAND) {
                this.player.clearActiveItem();
            }

            this.player.getInventory().setSelectedSlot(packet.getSelectedSlot());
            this.player.updateLastActionTime();
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onPlayerAction", cancellable = true)
    private void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        PlayerActionC2SPacket.Action action = packet.getAction();

        if (action == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND
        || action == PlayerActionC2SPacket.Action.DROP_ITEM) {
            if (WandWeaver.SPECIAL_ITEM_SLOTS.contains(this.player.getInventory().getSelectedSlot())) {
                ci.cancel();
            }
        }
    }
}
