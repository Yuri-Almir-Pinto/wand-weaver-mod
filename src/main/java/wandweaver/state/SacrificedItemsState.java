package wandweaver.state;

import com.mojang.datafixers.FunctionType;
import com.mojang.serialization.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import wandweaver.WandWeaver;

import java.util.*;

public class SacrificedItemsState extends PersistentState {
    public static final Codec<SacrificedItemsState> CODEC = NbtCompound.CODEC.xmap(
            (FunctionType<NbtCompound, SacrificedItemsState>) nbt -> {
                SacrificedItemsState state = new SacrificedItemsState();
                state.ensureMapNotNull();

                nbt.forEach((key, value) -> {
                    UUID uKey = UUID.fromString(key);
                    if (!state.sacrificedItems.containsKey(uKey)) {
                        state.sacrificedItems.put(uKey, new HashSet<>());
                    }

                    Set<Item> set = state.sacrificedItems.get(uKey);

                    int[] itemIds = value.asIntArray().orElseGet(() -> new int[0]);

                    for (int id : itemIds) {
                        set.add(Item.byRawId(id));
                    }
                });

                return state;
            },
            (FunctionType<SacrificedItemsState, NbtCompound>) state -> {
                NbtCompound nbt = new NbtCompound();
                state.ensureMapNotNull();

                state.sacrificedItems.forEach((key, value) -> {
                    String sKey = key.toString();
                    ArrayList<Integer> ids = new ArrayList<>();

                    value.forEach(i -> ids.add(Item.getRawId(i)));

                    nbt.putIntArray(sKey, ids.stream().mapToInt(Integer::intValue).toArray());
                });

                return nbt;
            }
    );

    private static final PersistentStateType<SacrificedItemsState> TYPE =
            new PersistentStateType<>(WandWeaver.MOD_ID, SacrificedItemsState::new, CODEC, null);

    public static SacrificedItemsState getState(MinecraftServer server) {
        return Objects.requireNonNull(server.getWorld(World.OVERWORLD))
                .getPersistentStateManager()
                .getOrCreate(TYPE);
    }

    private Map<UUID, Set<Item>> sacrificedItems;



    public boolean hasItem(PlayerEntity player, Item item) {
        ensureMapNotNull();

        UUID id = player.getGameProfile().getId();

        Set<Item> set = this.sacrificedItems.computeIfAbsent(id, k -> new HashSet<>());

        return set.contains(item);
    }

    public boolean putItem(PlayerEntity player, Item item) {
        ensureMapNotNull();

        UUID id = player.getGameProfile().getId();

        Set<Item> set = this.sacrificedItems.computeIfAbsent(id, k -> new HashSet<>());

        if (set.contains(item)) {
            return false;
        }

        set.add(item);

        markDirty();
        return true;
    }

    private void ensureMapNotNull() {
        if (this.sacrificedItems == null) {
            this.sacrificedItems = new HashMap<>();
        }
    }
}
