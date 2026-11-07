# Porting notes

This is the NeoForge 1.21.1 port of [Nekoma's Fixed](https://github.com/GreenJAB/nekomas-fixed) (MIT).

Sources it is built from:

- `upstream 1.21.1` (Fabric, branch `1.21.1`, cb1d1c9d), the base of this tree. Mojang names, 1.21.1 APIs.
- `upstream main` (0733f7ea, 0.5.5 for 26.3), the feature target. Features only `main` has are brought back here.

The tools expect a checkout of upstream `main` at `../nekomas-main` (or set `NEKOMA_MAIN`). NeoForge's patched Minecraft
source comes with the build: `build/moddev/artifacts/neoforge-*-sources.jar`.

## Tree

- `src/main/java`, `src/main/resources`, `src/main/generated`: common code and data. `generated` holds upstream's committed
  datagen output, and it's plain resources here since datagen isn't run on NeoForge.
- `src/client/java`: client code, compiled into the main source set. Keep every client class reachable only from
  `NekomasFixedClient`, a client mixin or a guarded call, so that dedicated servers never load it.
- `src/gametest`: GameTests. `src/smoke`: the hidden self-driving client. Neither is in the jar.
- `tools/backport_res.py`: copies resources from upstream main, converting formats. `tools/smoke.sh`: runs the hidden client.

## Bringing a feature back from main

26.3 code doesn't compile on 1.21.1, so re-express it. When a class exists on both upstream branches, compare them: that
shows how upstream's own developers made the same change. Typical changes:

| 26.3 (main) | 1.21.1 (here) |
|---|---|
| `Identifier` | `ResourceLocation` (`NekomasFixed.id(path)` works on both) |
| `ValueInput` / `ValueOutput` | `CompoundTag` in `loadAdditional(tag, registries)` / `saveAdditional(tag, registries)` |
| `addAdditionalSaveData(ValueOutput)` | `addAdditionalSaveData(CompoundTag)` |
| render states, `SubmitNodeCollector`, `extractRenderState`, `submit` | `EntityRenderer.render(entity, yaw, partialTick, poseStack, buffers, light)`, `BlockEntityRenderer.render(...)` directly; no render state classes |
| `items/*.json` item definitions, `SpecialModelRenderer`, `ItemModel` | `models/item/*.json` with overrides, `ItemProperties`, or a `BlockEntityWithoutLevelRenderer` through `IClientItemExtensions` |
| equipment assets (`equipment/*.json`) | `ArmorMaterial` layers with `textures/models/armor/*_layer_1/2.png` |
| `RecipeInput`, `RecipePropertySet`, `RecipeDisplay`, `RecipeBookCategory` | the 1.21.1 `Recipe` API; a `RecipeBookType` extension only if the feature needs a book |
| `TagValueInput`, `EntityReference`, `AttributeResolver` | plain tags, UUIDs, `getAttribute` |
| `Holder`-based `Item.Properties.setId`, `useItemDescriptionPrefix` | not needed, ids come from registration |
| `InteractionResult.SUCCESS_SERVER`, `TRY_WITH_EMPTY_HAND` | `ItemInteractionResult` for `useItemOn`, `InteractionResult` for `useWithoutItem` |
| data-driven villager trades | `VillagerTradesEvent` / `WandererTradesEvent` |
| Fabric events and registries | NeoForge events, see `NekomasFixed` and `NekomasFixedClient` |

Rules:

- **Registration**: registries are written eagerly from `init()` inside `RegisterEvent` (see `NekomasFixed`). New content goes in its own
  class under `registry/registries` with a `public static void register()` called from `init()`. Static fields that register must be
  forced to load from there. Anything that loads later than `init()` can't register.
- **Creative tabs**: add items through `ItemGroupRegistry.MAIN_EXTRAS` or `ItemGroupRegistry.COLOUR_EXTRAS`.
- **Client registration**: add a static method to the feature's own client class and call it from `NekomasFixedClient`'s event listeners.
- **Mixins**: list them in `nekomasfixed.mixins.json` or `nekomasfixed.client.mixins.json`. Read the target in NeoForge's patched source, because
  NeoForge patches many methods. A name-only `method =` picks the first overload, so give the full descriptor when there is more than one.
- **Biomes**: JSON in `data/nekomasfixed/neoforge/biome_modifier/`.
- **Names** of blocks, items and entities stay upstream main's, so existing lang keys and textures keep working. Lang text is handled
  centrally: `en_us.json` already has every main key.

## Left out on purpose

These depend on vanilla content that 1.21.1 doesn't have, or on a change that would be unsafe here:

- **Banners, firework-star names and other features that need new `DyeColor` values** for amber, aqua, indigo and maroon. `main` adds enum
  constants by mixin. Other mods iterate `DyeColor.values()` and can crash on them, so this port keeps upstream 1.21.1's separate
  colour system (`ModColors`, `AllDyes`). Firework stars can still take those colours as plain RGB.
- **Dyed bundles and harnesses, cushions**: 1.21.1 has no such base items.
- **Spears**, including the throwable spear, lunge changes and the Dismount enchantment: vanilla spears arrived in 1.21.11.
- **Baobab shelf**: vanilla shelves arrived in 1.21.9.
- **Copper armour** (copper goat horn helmet, copper lightning buff): vanilla copper armour arrived in 1.21.9.
- **Pale oak and poplar variants** of boats and hollow logs, **eyeblossom flower crowns**: those woods and flowers don't exist in 1.21.1.

## Made to fit 1.21.1

- **Sulfur** items exist, but vanilla sulfur doesn't: their recipes use gunpowder and their gas particle is the mod's own.
- **Option screens** use vanilla widgets instead of Cloth Config, so no extra mod is needed. Copper-armour options are gone with the armour.
- **Copper crown** is crafted from a leather helmet, since there is no copper helmet.
- **Quiver tooltip** is drawn with plain rectangles, because the sprites it uses on `main` are newer vanilla assets.
- **Pyrotechnist houses** join the village pools at server start instead of replacing vanilla's pool files, so they coexist with other mods' houses.
- **Wool and concrete slabs and stairs** for vanilla colours live under `nekomasfixed:`, as vanilla 1.21.1 has none.

## Testing

- `gradlew runGameTestServer` runs the GameTests in `src/gametest`.
- `tools/smoke.sh SCRIPT` runs the hidden self-driving client in `src/smoke`, reports missing models and names, and takes screenshots.
- `tools/gen_pt_br.py VANILLA_PT_BR.json` regenerates `pt_br.json` from its table and fails if an English key has no translation.
- `tools/parity_audit.py` lists what upstream `main` has that this tree lacks.
