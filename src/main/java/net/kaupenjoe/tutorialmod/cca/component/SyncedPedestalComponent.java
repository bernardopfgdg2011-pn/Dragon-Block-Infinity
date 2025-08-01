package net.kaupenjoe.tutorialmod.cca.component;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.entity.custom.PedestalBlockEntity;
import net.kaupenjoe.tutorialmod.cca.TutorialModCCAComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;

import java.util.function.Consumer;

public interface SyncedPedestalComponent extends Component, ClientTickingComponent {
    Identifier IDENTIFIER = Identifier.of(TutorialMod.MOD_ID, "synced_pedestal");

    static SyncedPedestalComponent get(PedestalBlockEntity blockEntity) {
        return TutorialModCCAComponents.SYNCED_BLOCK_INVENTORY.get(blockEntity);
    }

    /**
     * For proper Inventory modification, use {@link #modifyInventory(Consumer, boolean)}
     */
    DefaultedList<ItemStack> getInventory();

    void modifyInventory(Consumer<DefaultedList<ItemStack>> consumer, boolean shouldSync);

    ItemStack remove(int slot, @Nullable Integer amount, boolean shouldSync);

    default boolean hasContent() {
        for (ItemStack stack : getInventory()) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    float getRenderingRotation(float tickDelta);

    void sync();
}
