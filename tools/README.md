# tools

Developer utilities. Not part of the mod build.

## texgen.js

Procedurally generates the block and item textures under
`src/main/resources/assets/endplus/textures/`. Pure Node (no dependencies).

```bash
# write the real 16x16 PNGs straight into the resource tree
node tools/texgen.js src/main/resources/assets/endplus/textures

# also emit 10x preview PNGs + contact sheets for review
node tools/texgen.js src/main/resources/assets/endplus/textures /tmp/tex-preview
```

Each texture is a function in the `BLOCK` / `ITEM` maps, keyed by file name.
Colours come from the shared `PAL` table and the `endStone` / `woodSide` /
`woodTop` / `crackField` helpers, so related blocks (the umbral wood set, the
ore set, the void-stone set) stay visually consistent — change a `PAL` entry
and every member of that family moves with it.

Entity skins are **not** generated here; they are UV-mapped to the models in
`src/client/java/com/endplus/client/model/` and are authored by hand.
