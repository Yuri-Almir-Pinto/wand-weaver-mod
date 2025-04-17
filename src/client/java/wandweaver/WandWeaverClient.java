package wandweaver;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import wandweaver.networking.WandWeavingClientNetworking;
import wandweaver.rendering.SpellcastingRenderer;
import wandweaver.spells.ISpell;
import wandweaver.utils.Direction;
import wandweaver.utils.SpellcastingManager;

import java.util.concurrent.CopyOnWriteArrayList;

public class WandWeaverClient implements ClientModInitializer {
	// CopyOnWriteArrayList to avoid some... ConcurrentModification Exceptions. Mutating this list is not
	// frequent anyway.
	public static final CopyOnWriteArrayList<Direction> DIRECTION_LIST = new CopyOnWriteArrayList<>();
	public static int leftProgress = 0;
	public static int rightProgress = 0;
	public static int upProgress = 0;
	public static int downProgress = 0;
	public static ISpell currentSpell = null;

	@Override
	public void onInitializeClient() {
		SpellcastingRenderer.register(WandWeaverClient::renderArrows);
		SpellcastingManager.register();
		WandWeavingClientNetworking.registerClientHandlers();
	}

	public static void renderArrows(DrawContext context, RenderTickCounter tickDelta) {
		if (!WandWeaver.isCasting) {
			return;
		}

		SpellcastingRenderer.renderArrowsOnScreen(
				context,
				WandWeaverClient.DIRECTION_LIST,
				WandWeaverClient.leftProgress,
				WandWeaverClient.rightProgress,
				WandWeaverClient.upProgress,
				WandWeaverClient.downProgress
		);

		if (currentSpell != null) {
			SpellcastingRenderer.renderSpellOnScreen(context, currentSpell);
		}
	}
}
