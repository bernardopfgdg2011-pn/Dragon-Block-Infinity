Android native layout:

- `natives/android/<abi>/libfbxanim_native.so`

Supported ABIs:

- `arm64-v8a`
- `armeabi-v7a`
- `x86_64`
- `x86`

Optional bundled Assimp dependencies per ABI:

- `natives/android/<abi>/libassimp.so`
- `natives/android/<abi>/libdraco.so`

Use `native/build_android.sh` (Linux/macOS host) or `native/build_android.bat`
(Windows host) to compile `libfbxanim_native.so`.
