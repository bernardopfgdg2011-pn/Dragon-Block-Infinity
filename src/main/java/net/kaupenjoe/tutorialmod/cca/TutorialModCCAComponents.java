package net.kaupenjoe.tutorialmod.cca;

import net.kaupenjoe.tutorialmod.block.entity.custom.PedestalBlockEntity;
import net.kaupenjoe.tutorialmod.cca.component.SyncedPedestalComponent;
import net.kaupenjoe.tutorialmod.cca.implementation.SyncedPedestalComponentImpl;
import org.ladysnake.cca.api.v3.block.BlockComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.block.BlockComponentInitializer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

public class TutorialModCCAComponents implements BlockComponentInitializer {
    public static final ComponentKey<SyncedPedestalComponent> SYNCED_BLOCK_INVENTORY =
            ComponentRegistry.getOrCreate(SyncedPedestalComponent.IDENTIFIER, SyncedPedestalComponent.class);

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(PedestalBlockEntity.class, SYNCED_BLOCK_INVENTORY, SyncedPedestalComponentImpl::new);
    }
}
