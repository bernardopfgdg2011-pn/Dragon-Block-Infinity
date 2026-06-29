package net.dragonblockinfinity;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonBlockInfinity implements ModInitializer {
    public static final String MOD_ID = "DragonBlockInfinity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Dragon Block Infinity initialized!");
    }
}
