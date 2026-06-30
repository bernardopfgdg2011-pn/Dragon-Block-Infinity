package com.brckv2.objfbxloader.fabric;

import com.brckv2.objfbxloader.ObjFbxLoader;
import net.fabricmc.api.ModInitializer;

public final class ObjFbxLoaderFabricEntrypoint implements ModInitializer {
   public void onInitialize() {
      new ObjFbxLoader().onInitialize();
   }
}
