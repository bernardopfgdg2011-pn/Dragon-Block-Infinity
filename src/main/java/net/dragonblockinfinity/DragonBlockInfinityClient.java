package net.dragonblockinfinity;

import net.fabricmc.api.ClientModInitializer;
import net.dragonblockinfinity.render.obj.Hair;

public class DragonBlockInfinityClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client initialization logic goes here.
        Hair.register();
    }
}
