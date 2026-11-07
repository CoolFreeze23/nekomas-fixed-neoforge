package net.greenjab.nekomasfixed;

import net.greenjab.nekomasfixed.network.SyncHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.greenjab.nekomasfixed.screen.config.ModConfigValues;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.registries.Registries;
import net.greenjab.nekomasfixed.registry.block.cauldron.CauldronBehaviour;
import net.greenjab.nekomasfixed.registry.registries.*;
import net.greenjab.nekomasfixed.registry.worldgen.ModWorldGeneration;
import net.greenjab.nekomasfixed.util.ModTreeDecorators;
import net.greenjab.nekomasfixed.util.ModTrunkPlacers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.greenjab.nekomasfixed.util.options.EntitySpawnOption;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.greenjab.nekomasfixed.registry.entity.TargetDummy;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NekomasFixed.NAMESPACE)
public class NekomasFixed {
    public static final String MOD_NAME = "Nekomas' Fixed Minecraft";
    public static final String NAMESPACE = "nekomasfixed";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public static final int ECHOING_LAYER_LIMIT = 8;

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    // Sums the level of any enchantment whose id contains the given name (e.g. "leeching").
    public static int enchantLevel(ItemStack stack, String name) {
        int level = 0;
        ItemEnchantments components = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : stack.getEnchantments().keySet()) {
            if (holder.getRegisteredName().toLowerCase().contains(name.toLowerCase())) {
                level += components.getLevel(holder);
            }
        }
        return level;
    }

    public NekomasFixed(IEventBus modBus) {
        modBus.addListener(NekomasFixed::onRegister);
        modBus.addListener(SyncHandler::register);
        modBus.addListener(BlockEntityRegistry::attachBlockEntities);
        modBus.addListener(SulfurRegistry::attachBlockEntities);
        modBus.addListener(EntityTypeRegistry::registerAttributes);
        modBus.addListener(EntityTypeRegistry::registerSpawnPlacements);
        modBus.addListener(TermiteRegistry::registerAttributes);
        modBus.addListener(CrownRegistry::modifyComponents);
        NeoForge.EVENT_BUS.addListener(GlowingDecoratedPotPatternRegistry::onUseBlock);
        NeoForge.EVENT_BUS.addListener(PotInteractions::onUseBlock);
        NeoForge.EVENT_BUS.addListener(LootTableModifierRegistry::onLootTableLoad);
        NeoForge.EVENT_BUS.addListener(BlockRegistry::onToolModification);
        NeoForge.EVENT_BUS.addListener(LootTableAdditions::onLootTableLoad);
        NeoForge.EVENT_BUS.addListener(LootTableAdditions::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(VillagerRegistry::onVillagerTrades);
        NeoForge.EVENT_BUS.addListener(NekomasFixed::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener((ExplosionKnockbackEvent event) -> {
            if (event.getAffectedEntity() instanceof TargetDummy) {
                event.setKnockbackVelocity(net.minecraft.world.phys.Vec3.ZERO);
            }
        });
        modBus.addListener(BoatRegistry::modifyDefaultComponents);
        NeoForge.EVENT_BUS.addListener(BoatRegistry::onLootTableLoad);
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) ->
                ModConfigValues.onServerStarting(event.getServer().getWorldPath(LevelResource.ROOT)));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> ModConfigValues.onServerStopped());
    }

    /**
     * Upstream registers eagerly from static initializers. All vanilla registries are unfrozen while
     * RegisterEvent fires, so everything runs at once when the entity type event comes up: by then
     * NeoForge's own attributes exist, which living entity attribute suppliers need.
     */
    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ENTITY_TYPE)) {
            init();
        }
    }

    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        // Entities turned off in this world's options don't spawn; ones already saved stay.
        if (!event.loadedFromDisk() && EntitySpawnOption.isDisabled(entity.getType())) {
            event.setCanceled(true);
            return;
        }
        // Monsters keep away from ocelots that trust a player.
        if (entity instanceof Monster monster && !(entity instanceof Creeper)) {
            monster.goalSelector.addGoal(1, new AvoidEntityGoal<>(monster, Ocelot.class,
                    target -> target instanceof Ocelot ocelot && ocelot.isTrusting(), 8.0F, 1.0D, 1.3D, living -> true));
        }
    }

    private static void init() {
        LOGGER.info("[{}] loading", MOD_NAME);
        ComponentRegistry.registerComponents();
        ArmorMaterialRegistry.registerArmorMaterials();
        EffectRegistry.registerEffects();
        ModTreeDecorators.register();
        ModTrunkPlacers.register();
        BlockRegistry.registerBlocks();
        CauldronBehaviour.register();
        ItemRegistry.registerItems();
        ScreenHandlerRegistry.registerScreenHandlers();
        EntityTypeRegistry.registerEntityType();
        BoatRegistry.register();
        BlockEntityTypeRegistry.registerBlockEntityTypes();
        RecipeRegistry.registerRecipes();
        EnchantmentRegistry.registerEnchantments();
        ItemGroupRegistry.registerItemGroup();
        ModWorldGeneration.generateModWorldGen();
        SoundEventRegistry.registerSoundEvents();
        GlowingDecoratedPotPatternRegistry.registerGlowingDecoratedPotPatterns();
        VillagerRegistry.registerVillagers();
        TermiteRegistry.register();
        TrimRegistry.register();
        CrownRegistry.register();
        GoatHornRegistry.register();
        ColourBlockRegistry.register();
        PotRegistry.register();
        SoupRegistry.register();
        SulfurRegistry.register();
        BrushRegistry.register();
    }
}
