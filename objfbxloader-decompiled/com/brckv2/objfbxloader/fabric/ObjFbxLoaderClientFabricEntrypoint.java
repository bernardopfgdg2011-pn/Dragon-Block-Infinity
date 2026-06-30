package com.brckv2.objfbxloader.fabric;

import com.brckv2.objfbxloader.ObjFbxLoaderClient;
import net.fabricmc.api.ClientModInitializer;

public final class ObjFbxLoaderClientFabricEntrypoint implements ClientModInitializer {
   public void onInitializeClient() {
      new ObjFbxLoaderClient().onInitializeClient();
   }
}
