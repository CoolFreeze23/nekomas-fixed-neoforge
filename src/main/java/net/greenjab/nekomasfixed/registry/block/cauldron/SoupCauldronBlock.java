package net.greenjab.nekomasfixed.registry.block.cauldron;

import com.mojang.serialization.MapCodec;
import net.greenjab.nekomasfixed.registry.block.entity.SoupCauldronBlockEntity;
import net.greenjab.nekomasfixed.registry.registries.ItemRegistry;
import net.greenjab.nekomasfixed.registry.registries.SoupRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SoupCauldronBlock extends BaseEntityBlock {
    public static final MapCodec<SoupCauldronBlock> CODEC = simpleCodec(SoupCauldronBlock::new);
    private static final VoxelShape RAYCAST_SHAPE = Block.box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape OUTLINE_SHAPE = Util.make(() -> Shapes.join(
            Shapes.block(),
            Shapes.or(Block.box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), Block.box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), RAYCAST_SHAPE),
            BooleanOp.ONLY_FIRST));

    public SoupCauldronBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return Items.CAULDRON.getDefaultInstance();
    }

    public static boolean isHeated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.FIRE) || below.is(BlockTags.CAMPFIRES);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SoupCauldronBlockEntity be)) {
            return ItemInteractionResult.FAIL;
        } else if (stack.is(Items.STICK) && isHeated(level, pos)) {
            if (be.hasStirred) return ItemInteractionResult.FAIL;
            be.setStirred(level);
            if (level.isClientSide()) {
                RandomSource random = level.getRandom();
                for (int i = 0; i < 4; i++) {
                    level.addAlwaysVisibleParticle(ParticleTypes.POOF, true,
                            pos.getX() + 0.5 + random.nextDouble() * (random.nextBoolean() ? 1 : -1), pos.getY() + 1.0,
                            pos.getZ() + 0.5 + random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                            0.001 * (random.nextBoolean() ? 1 : -1), 0.0001, 0.001 * (random.nextBoolean() ? 1 : -1));
                }
            }
            return ItemInteractionResult.SUCCESS;
        } else if (FOOD_COLORS.containsKey(stack.getItem()) && isHeated(level, pos)) {
            if (be.hasStirred || be.getInputs().size() >= 4) return ItemInteractionResult.FAIL;
            if (!level.isClientSide()) {
                if (be.addInput(stack)) stack.consume(1, player);
            }
            level.updateNeighbourForOutputSignal(pos, this);
            return ItemInteractionResult.SUCCESS;
        } else if (stack.is(Items.BOWL)) {
            if (!be.hasStirred) return ItemInteractionResult.FAIL;
            if (!level.isClientSide()) {
                ItemStack soup = new ItemStack(SoupRegistry.SPECIAL_STEW);
                List<ItemStack> copiedInputs = be.getInputs().stream().map(ItemStack::copy).toList();
                soup.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(copiedInputs));
                soup.set(DataComponents.DYED_COLOR, new DyedItemColor(blendFoodColors(copiedInputs), false));
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, soup));
                level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                for (ItemStack ingredient : copiedInputs) {
                    ItemStack remainder = remainderOf(ingredient);
                    if (!remainder.isEmpty()) Block.popResource(level, pos, remainder);
                }
            }
            return ItemInteractionResult.SUCCESS;
        } else if (stack.isEmpty()) {
            if (be.hasStirred) return ItemInteractionResult.FAIL;
            if (!level.isClientSide()) {
                player.setItemInHand(hand, be.removeInput());
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.FAIL;
    }

    /** What a used ingredient leaves behind: the bowl, bottle or bucket it came in. */
    private static ItemStack remainderOf(ItemStack ingredient) {
        FoodProperties food = ingredient.get(DataComponents.FOOD);
        if (food != null && food.usingConvertsTo().isPresent()) return food.usingConvertsTo().get().copy();
        if (ingredient.has(DataComponents.POTION_CONTENTS)) return new ItemStack(Items.GLASS_BOTTLE);
        return ingredient.hasCraftingRemainingItem() ? ingredient.getCraftingRemainingItem() : ItemStack.EMPTY;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoupCauldronBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, SoupRegistry.SOUP_CAULDRON_BLOCK_ENTITY, SoupCauldronBlockEntity::clientTick) : null;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        int hunger = 0;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SoupCauldronBlockEntity soup) {
            for (ItemStack item : soup.getInputs()) {
                FoodProperties food = SoupCauldronBlockEntity.cooked(item, level).get(DataComponents.FOOD);
                if (food != null) hunger += Mth.ceil(food.nutrition() / 2f);
            }
        }
        return hunger;
    }

    public static final Map<Item, Integer> FOOD_COLORS = Map.ofEntries(
            Map.entry(Items.POTION, 0x385DC6),
            Map.entry(Items.APPLE, 0xFC1C2A),
            Map.entry(Items.GOLDEN_APPLE, 0xE7EB56),
            Map.entry(Items.ENCHANTED_GOLDEN_APPLE, 0xE7EB56),
            Map.entry(Items.MELON_SLICE, 0xBD3023),
            Map.entry(Items.SWEET_BERRIES, 0xA30700),
            Map.entry(Items.GLOW_BERRIES, 0xF4DF6A),
            Map.entry(Items.CHORUS_FRUIT, 0x8C668B),
            Map.entry(Items.CARROT, 0xFC8C09),
            Map.entry(Items.GOLDEN_CARROT, 0xE7EB56),
            Map.entry(Items.POTATO, 0xD6A850),
            Map.entry(Items.BAKED_POTATO, 0xD6A850),
            Map.entry(Items.BEETROOT, 0xA2272B),
            Map.entry(Items.KELP, 0x3B3224),
            Map.entry(Items.DRIED_KELP, 0x3B3224),
            Map.entry(Items.BEEF, 0x703E2C),
            Map.entry(Items.COOKED_BEEF, 0x703E2C),
            Map.entry(Items.PORKCHOP, 0xD0BE86),
            Map.entry(Items.COOKED_PORKCHOP, 0xD0BE86),
            Map.entry(Items.MUTTON, 0x804739),
            Map.entry(Items.COOKED_MUTTON, 0x804739),
            Map.entry(Items.CHICKEN, 0xCB7C49),
            Map.entry(Items.COOKED_CHICKEN, 0xCB7C49),
            Map.entry(Items.RABBIT, 0xCF8C61),
            Map.entry(Items.COOKED_RABBIT, 0xCF8C61),
            Map.entry(Items.COD, 0xD3C3AB),
            Map.entry(Items.COOKED_COD, 0xD3C3AB),
            Map.entry(Items.SALMON, 0xB84E23),
            Map.entry(Items.COOKED_SALMON, 0xB84E23),
            Map.entry(Items.TROPICAL_FISH, 0xF16E20),
            Map.entry(Items.MILK_BUCKET, 0xFCFCFC),
            Map.entry(Items.HONEY_BOTTLE, 0xFC8F16),
            Map.entry(ItemRegistry.BAOBAB_FRUIT, 0x686D24));

    public static Optional<Integer> getFoodColor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) return Optional.of(contents.getColor());

        Integer foodColor = FOOD_COLORS.get(stack.getItem());
        if (foodColor != null) return Optional.of(foodColor);

        return Optional.empty();
    }

    public static int blendFoodColors(List<ItemStack> items) {
        int totalR = 0;
        int totalG = 0;
        int totalB = 0;
        int totalWeight = 0;

        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            Optional<Integer> colorOpt = getFoodColor(stack);
            if (colorOpt.isEmpty()) continue;
            int color = colorOpt.get();
            totalR += color >> 16 & 255;
            totalG += color >> 8 & 255;
            totalB += color & 255;
            totalWeight++;
        }
        if (totalWeight == 0) return 0x385DC6;

        return (totalR / totalWeight << 16) | (totalG / totalWeight << 8) | totalB / totalWeight;
    }
}
