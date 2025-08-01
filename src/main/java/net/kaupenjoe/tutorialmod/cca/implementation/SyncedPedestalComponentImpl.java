package net.kaupenjoe.tutorialmod.cca.implementation;

import net.kaupenjoe.tutorialmod.block.entity.custom.PedestalBlockEntity;
import net.kaupenjoe.tutorialmod.cca.TutorialModCCAComponents;
import net.kaupenjoe.tutorialmod.cca.component.SyncedPedestalComponent;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.function.Consumer;

public class SyncedPedestalComponentImpl implements SyncedPedestalComponent, AutoSyncedComponent {
    private final PedestalBlockEntity provider;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float renderingRotation;


    public SyncedPedestalComponentImpl(PedestalBlockEntity provider) {
        this.provider = provider;
        this.renderingRotation = 0;
    }

    @Override
    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void modifyInventory(Consumer<DefaultedList<ItemStack>> consumer, boolean shouldSync) {
        consumer.accept(this.inventory);
        if (shouldSync) {
            this.sync();
        }
    }

    @Override
    public ItemStack remove(int slot, @Nullable Integer amount, boolean shouldSync) {
        ItemStack result;
        if (amount == null) {
            result = Inventories.removeStack(this.inventory, slot);
        } else {
            result = Inventories.splitStack(this.inventory, slot, amount);
        }
        if (shouldSync) {
            this.sync();
        }
        return result;
    }

    @Override
    public float getRenderingRotation(float tickDelta) {
        return this.renderingRotation + tickDelta;
    }

    @Override
    public void clientTick() {
        this.renderingRotation += 0.5f;
        if (this.renderingRotation >= 360) {
            this.renderingRotation = 0;
        }
    }

    @Override
    public void sync() {
        if (!(this.provider.getWorld() instanceof ServerWorld)) return;
        TutorialModCCAComponents.SYNCED_BLOCK_INVENTORY.sync(this.provider);
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory, registryLookup);
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
    }
}
