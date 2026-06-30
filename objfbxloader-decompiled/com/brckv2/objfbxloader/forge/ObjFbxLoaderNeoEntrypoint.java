package com.brckv2.objfbxloader.forge;

import net.neoforged.fml.common.Mod;

@Mod("objfbxloader")
public final class ObjFbxLoaderNeoEntrypoint {
   public ObjFbxLoaderNeoEntrypoint() {
      ObjFbxLoaderLoaderBootstrap.bootstrapClientIfPresent();
   }
}
