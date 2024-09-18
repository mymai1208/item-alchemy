package net.pitan76.itemalchemy.tile;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.itemalchemy.EMCManager;
import net.pitan76.itemalchemy.gui.screen.EMCImporterScreenHandler;
import net.pitan76.itemalchemy.tile.base.OwnedBlockEntity;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.container.factory.DisplayNameArgs;
import net.pitan76.mcpitanlib.api.event.container.factory.ExtraDataArgs;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.event.tile.TileTickEvent;
import net.pitan76.mcpitanlib.api.gui.ExtendedScreenHandlerFactory;
import net.pitan76.mcpitanlib.api.gui.inventory.IInventory;
import net.pitan76.mcpitanlib.api.tile.ExtendBlockEntityTicker;
import net.pitan76.mcpitanlib.api.util.*;
import org.jetbrains.annotations.Nullable;

public class EMCImporterTile extends OwnedBlockEntity implements ExtendBlockEntityTicker<EMCImporterTile>, SidedInventory, IInventory, ExtendedScreenHandlerFactory {

    public DefaultedList<ItemStack> filter = DefaultedList.ofSize(9, ItemStackUtil.empty());
    public DefaultedList<ItemStack> inv = DefaultedList.ofSize(1, ItemStackUtil.empty());

    public EMCImporterTile(BlockEntityType<?> type, TileCreateEvent e) {
        super(type, e);
    }

    public EMCImporterTile(TileCreateEvent e) {
        this(Tiles.EMC_IMPORTER.getOrNull(), e);
    }

    @Override
    public void writeNbt(WriteNbtArgs args) {
        NbtCompound filterNbt = NbtUtil.create();
        InventoryUtil.writeNbt(args.registryLookup, filterNbt, filter);

        NbtCompound invNbt = NbtUtil.create();
        InventoryUtil.writeNbt(args.registryLookup, invNbt, inv);

        NbtUtil.put(args.nbt, "filter", filterNbt);
        NbtUtil.put(args.nbt, "inv", invNbt);

        if (teamUUID != null)
            NbtUtil.putUuid(args.nbt, "team", teamUUID);
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        if (NbtUtil.has(args.nbt, "filter")) {
            NbtCompound filterNbt = NbtUtil.get(args.nbt, "filter");
            InventoryUtil.readNbt(args.registryLookup, filterNbt, filter);
        }

        if (NbtUtil.has(args.nbt, "inv")) {
            NbtCompound invNbt = NbtUtil.get(args.nbt, "inv");
            InventoryUtil.readNbt(args.registryLookup, invNbt, inv);
        }

        if (NbtUtil.has(args.nbt, "team"))
            teamUUID = NbtUtil.getUuid(args.nbt, "team");
    }

    @Nullable
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new EMCImporterScreenHandler(syncId, inv, this, this);
    }

    @Override
    public void tick(TileTickEvent<EMCImporterTile> e) {
        World world = e.world;
        if (WorldUtil.isClient(world)) return;

        if (!hasTeam()) return;
        if (inv.get(0).isEmpty()) return;

        ItemStack stack = inv.get(0);
        long emc = EMCManager.get(stack);
        if (emc <= 0) return;

        if (!filter.isEmpty()) {
            boolean isFiltered = false;
            for (ItemStack filterStack : filter) {
                if (ItemUtil.isEqual(stack.getItem(), filterStack.getItem())) {
                    isFiltered = true;
                    break;
                }
            }
            if (!isFiltered)
                return;
        }

        getTeamState().get().storedEMC += emc;
        inv.set(0, ItemStackUtil.empty());
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inv;
    }

    public int getFilterCount() {
        int count = 0;
        for (int i = filter.size() - 1; i >= 0; i--) {
            if (!filter.get(i).isEmpty())
                count++;
        }
        return count;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] result = new int[getItems().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (!EMCManager.contains(stack.getItem()))
            return false;

        if (!filter.isEmpty()) {
            boolean isFiltered = false;
            for (ItemStack filterStack : filter) {
                if (ItemUtil.isEqual(stack.getItem(), filterStack.getItem())) {
                    isFiltered = true;
                    break;
                }
            }
            if (!isFiltered)
                return false;
        }

        return dir != Direction.DOWN;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN;
    }

    @Override
    public Text getDisplayName(DisplayNameArgs args) {
        return TextUtil.translatable("block.itemalchemy.emc_importer");
    }

    @Override
    public void writeExtraData(ExtraDataArgs args) {
        NbtCompound data = NbtUtil.create();
        NbtUtil.putInt(data, "x", pos.getX());
        NbtUtil.putInt(data, "y", pos.getY());
        NbtUtil.putInt(data, "z", pos.getZ());
        if (teamUUID != null)
            NbtUtil.putUuid(data, "team", teamUUID);

        args.writeVar(data);
    }
}
