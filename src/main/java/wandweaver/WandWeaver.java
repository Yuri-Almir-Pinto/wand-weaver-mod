package wandweaver;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wandweaver.items.ItemsManager;
import wandweaver.network.WandWeaverNetworking;
import wandweaver.spells.SpellManager;

public class WandWeaver implements ModInitializer {
	public static final String MOD_ID = "wand-weaver";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean isCasting = false;
	public static boolean isAutoCasting = false;


	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Wand Weaver!");

		WandWeaverNetworking.registerPayloads();
		WandWeaverNetworking.registerServerHandlers();

		// Initialize items
		ItemsManager.initialize();

		// Spell registration
		SpellManager.register();
	}
}