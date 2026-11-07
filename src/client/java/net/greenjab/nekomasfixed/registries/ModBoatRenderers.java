package net.greenjab.nekomasfixed.registries;

import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.registry.entity.BigBoat;
import net.greenjab.nekomasfixed.registry.entity.HugeBoat;
import net.greenjab.nekomasfixed.registry.registries.BoatRegistry;
import net.greenjab.nekomasfixed.render.entity.BigBoatRenderer;
import net.greenjab.nekomasfixed.render.entity.HugeBoatRenderer;
import net.greenjab.nekomasfixed.render.entity.model.BigBoatModel;
import net.greenjab.nekomasfixed.render.entity.model.HugeBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModBoatRenderers {

    private static ModelLayerLocation layer(EntityType<?> type, String size) {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
        String wood = path.substring(size.length() + 1, path.length() - "_boat".length());
        return new ModelLayerLocation(NekomasFixed.id(size + "_boat/" + wood), "main");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (EntityType<BigBoat> type : BoatRegistry.bigBoats) {
            event.registerLayerDefinition(layer(type, "big"), BigBoatModel::getChestTexturedModelData);
        }
        for (EntityType<HugeBoat> type : BoatRegistry.hugeBoats) {
            event.registerLayerDefinition(layer(type, "huge"), HugeBoatModel::getChestTexturedModelData);
        }
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BoatRegistry.FAKE_BOAT, NoopRenderer::new);
        for (EntityType<BigBoat> type : BoatRegistry.bigBoats) {
            ModelLayerLocation location = layer(type, "big");
            event.registerEntityRenderer(type, context -> new BigBoatRenderer<>(context, location));
        }
        for (EntityType<HugeBoat> type : BoatRegistry.hugeBoats) {
            ModelLayerLocation location = layer(type, "huge");
            event.registerEntityRenderer(type, context -> new HugeBoatRenderer(context, location));
        }
    }
}
