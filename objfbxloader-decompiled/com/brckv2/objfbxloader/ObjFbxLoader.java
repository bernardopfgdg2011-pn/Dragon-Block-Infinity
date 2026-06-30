package com.brckv2.objfbxloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjFbxLoader {
   public static final String MOD_ID = "objfbxloader";
   public static final Logger LOGGER = LoggerFactory.getLogger("objfbxloader");

   public void onInitialize() {
      LOGGER.info("OBJ/FBX Loader initialized.");
   }
}
