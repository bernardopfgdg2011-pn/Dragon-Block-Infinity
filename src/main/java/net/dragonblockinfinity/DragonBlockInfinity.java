package net.dragonblockinfinity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.client.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Very important comment
public class DragonBlockInfinity implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "DragonBlockInfinity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Mod initialization logic goes here.
    }

    @Override
    public void onInitializeClient() {
        // Client initialization logic goes here.
    }
}
