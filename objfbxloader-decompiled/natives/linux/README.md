Place Linux native libraries in this folder when packaging the mod:

- `libfbxanim_native.so` (built from `native/build_linux.sh`)
- Optional (if you want bundled Assimp dependencies):
  - `libassimp.so`
  - `libdraco.so`

If `libassimp.so`/`libdraco.so` are not bundled here, the loader will fall back
to system/LWJGL native resolution on Linux.
