package com.brckv2.objfbxloader.forge;

import net.minecraftforge.fml.common.Mod;

@Mod("objfbxloader")
public final class ObjFbxLoaderForgeEntrypoint {
   public ObjFbxLoaderForgeEntrypoint() {
      ObjFbxLoaderLoaderBootstrap.bootstrapClientIfPresent();
   }
}
