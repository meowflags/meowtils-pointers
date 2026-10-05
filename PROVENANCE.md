# Source and asset provenance

This release contains Pointers 1.29 and TenacityGUI 1.2.4 compatibility changes by @futyimoso, made with the help of GPT-6.1 Sol Ultra.

Pointers was developed from the user-supplied `PointersExtension-1.15.meowtils`. Its recovered Java source was edited and expanded; the existing pointer artwork and Nexa font were retained. Some source files retain decompiler comments from that recovery.

The TenacityGUI compatibility build was developed from the user-supplied `TenacityGUI-1.2.meowtils`. Five changed Java classes are included under `src/tenacity-compat`. They add support for optional extension preview hooks, conditional controls, and slider suffixes. The rest of the TenacityGUI implementation is retained in the built compatibility extension.

The bundled original components and external Meowtils/Minecraft/Forge libraries remain the work of their respective creators. No new license is assigned to third-party code or assets in this repository.

The showcase images were captured from the actual local renderer using the packaged pointer assets, not generated as substitute artwork.
