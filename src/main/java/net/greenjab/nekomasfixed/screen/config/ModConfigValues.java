package net.greenjab.nekomasfixed.screen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.greenjab.nekomasfixed.NekomasFixed;
import net.greenjab.nekomasfixed.util.options.EntitySpawnOption;
import net.greenjab.nekomasfixed.util.options.Option;
import net.greenjab.nekomasfixed.util.options.RecipeOption;
import net.greenjab.nekomasfixed.util.options.WorldGenOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class ModConfigValues {
    private static final Logger LOGGER = LoggerFactory.getLogger("nekomasfixed");

    private static Supplier<EntityType<?>> entity(String path) {
        return () -> BuiltInRegistries.ENTITY_TYPE.get(NekomasFixed.id(path));
    }

    public static final WorldGenOption<Boolean> generateGeysers = new WorldGenOption<>("generateGeysers", true);
    public static final WorldGenOption<Boolean> generateMound = new WorldGenOption<>("generateMound", true);

    public static final EntitySpawnOption spawnSusSpider = new EntitySpawnOption("spawnSusSpider", entity("suspicious_spider"), true);
    public static final EntitySpawnOption spawnMoobloom = new EntitySpawnOption("spawnMoobloom", entity("moobloom"), true);
    public static final EntitySpawnOption spawnTermite = new EntitySpawnOption("spawnTermite", entity("termite"), true);
    public static final EntitySpawnOption spawnRime = new EntitySpawnOption("spawnRime", entity("rime"), true);
    public static final EntitySpawnOption spawnDerelict = new EntitySpawnOption("spawnDerelict", entity("derelict"), true);
    public static final EntitySpawnOption spawnDrenched = new EntitySpawnOption("spawnDrenched", entity("drenched"), true);
    public static final EntitySpawnOption spawnWildfire = new EntitySpawnOption("spawnWildfire", entity("wildfire"), true);

    public static final Option<Boolean> enableMessyBeds = new Option<>("enableMessyBeds", true);
    public static final Option<Boolean> enableStackedCakes = new Option<>("enableStackedCakes", true);
    public static final Option<Integer> cakeSize = new Option<>("cakeSize", 3);

    public static final Option<Boolean> netherFoodRotting = new Option<>("netherFoodRotting", true);
    public static final Option<Boolean> magmaBlockNeedsSilkTouch = new Option<>("magmaBlockNeedsSilkTouch", true);

    public static final Option<Boolean> endermanHeadActivates = new Option<>("endermanHeadActivates", true);

    public static final RecipeOption dyedBrush =
            new RecipeOption("dyed_brush", path -> path.endsWith("_dyed_brush"), true);

    public static boolean isRecipeEnabled(ResourceLocation recipeId) {
        for (RecipeOption o : RecipeOption.ALL_RECIPES) {
            if (o.controls(recipeId)) return o.get();
        }
        return true;
    }

    public static boolean applyToNextWorld = false;
    private static Path currentWorldDir = null;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "nekomasfixed.json";

    public static boolean isWorldLoaded() {
        return currentWorldDir != null;
    }

    public static void resetToDefaults() {
        for (Option<?> o : Option.ALL) o.reset();
        applyToNextWorld = false;
    }

    public static void onServerStarting(Path worldDir) {
        currentWorldDir = worldDir;
        Path file = worldDir.resolve(FILE_NAME);

        if (Files.exists(file)) {
            try {
                JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                for (Option<?> o : Option.ALL) o.load(json, GSON);
            } catch (Exception e) {
                LOGGER.error("Failed to read {}", file, e);
            }
        } else {
            if (!applyToNextWorld) {
                resetToDefaults();
            }
            saveCurrentWorld();
            applyToNextWorld = false;
        }
    }

    public static void onServerStopped() {
        currentWorldDir = null;
        resetToDefaults();
    }

    public static void saveCurrentWorld() {
        if (currentWorldDir == null) return;
        JsonObject json = new JsonObject();
        for (Option<?> o : Option.ALL) o.save(json, GSON);
        try {
            Files.writeString(currentWorldDir.resolve(FILE_NAME), GSON.toJson(json));
        } catch (IOException e) {
            LOGGER.error("Failed to write config", e);
        }
    }
}
