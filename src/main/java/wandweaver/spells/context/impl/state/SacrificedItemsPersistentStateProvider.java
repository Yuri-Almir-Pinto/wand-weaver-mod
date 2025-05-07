package wandweaver.spells.context.impl.state;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import wandweaver.spells.ISacrificeListener;
import wandweaver.spells.SpellManager;
import wandweaver.spells.context.state.ISacrificedItemsState;
import wandweaver.state.SacrificedItemsState;

import java.util.Objects;
import java.util.Set;

public class SacrificedItemsPersistentStateProvider implements ISacrificedItemsState {
    private final ServerPlayerEntity player;
    private final SacrificedItemsState sacrificedItemsState;

    public SacrificedItemsPersistentStateProvider(ServerPlayerEntity player) {
        this.player = player;
        MinecraftServer server = Objects.requireNonNull(player.getServer());
        this.sacrificedItemsState = SacrificedItemsState.getState(server);
    }

    @Override
    public boolean canSacrificeItem(Item item) {
        Set<Item> acceptedSacrifices = SpellManager.getAcceptedSacrificeItems();

        return !this.sacrificedItemsState.hasItem(player, item) && acceptedSacrifices.contains(item);
    }

    @Override
    public boolean putItem(Item item) {
        return this.sacrificedItemsState.putItem(player, item);
    }

    @Override
    public boolean hasItem(Item item) {
        return this.sacrificedItemsState.hasItem(player, item);
    }

    @Override
    public Set<Item> allowedItems() {
        return SpellManager.getAcceptedSacrificeItems();
    }

    @Override
    public Set<ISacrificeListener> getListeners() {
        return SpellManager.getSacrificeListeners();
    }
}
