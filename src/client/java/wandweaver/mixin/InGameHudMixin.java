package wandweaver.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wandweaver.WandWeaver;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void disableCrosshairIfCasting(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (WandWeaver.isCasting || WandWeaver.isAutoCasting) {
            ci.cancel();
        }
    }
}
