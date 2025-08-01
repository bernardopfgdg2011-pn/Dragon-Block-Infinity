package net.kaupenjoe.tutorialmod.block.entity.custom;

import net.kaupenjoe.tutorialmod.block.entity.ModBlockEntities;
import net.kaupenjoe.tutorialmod.cca.component.SyncedPedestalComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity implements SidedInventory {
    private final SyncedPedestalComponent syncedComponent;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL_BE, pos, state);
        this.syncedComponent = SyncedPedestalComponent.get(this);
    }

    public SyncedPedestalComponent getSyncedComponent() {
        return syncedComponent;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] result = new int[syncedComponent.getInventory().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return dir == null || dir.equals(Direction.UP);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir == null || dir.equals(Direction.DOWN);
    }

    @Override
    public int size() {
        return syncedComponent.getInventory().size();
    }

    @Override
    public boolean isEmpty() {
        return !syncedComponent.hasContent();
    }

    @Override
    public ItemStack getStack(int slot) {
        return syncedComponent.getInventory().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.syncedComponent.remove(slot, amount, true);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.syncedComponent.remove(slot, null, true);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.syncedComponent.modifyInventory(itemStacks -> itemStacks.set(slot, stack), true);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.syncedComponent.modifyInventory(DefaultedList::clear, true);
    }
}
