package com.brckv2.objfbxloader.forge;

import com.brckv2.objfbxloader.ObjFbxLoader;

final class ObjFbxLoaderLoaderBootstrap {
   private ObjFbxLoaderLoaderBootstrap() {
   }

   static void bootstrapClientIfPresent() {
      ObjFbxLoader.LOGGER.info("Forge/Neo bootstrap entrypoint reached.");
      new ObjFbxLoader().onInitialize();

      try {
         ObjFbxLoader.LOGGER.info("Starting Forge/Neo client bootstrap thread.");
         ObjFbxLoaderNeoClientBootstrap.initialize();
         ObjFbxLoader.LOGGER.info("Forge/Neo client bootstrap initialized.");
      } catch (Throwable var1) {
         ObjFbxLoader.LOGGER.error("Forge/NeoForge client bootstrap failed.", var1);
      }
   }
}
