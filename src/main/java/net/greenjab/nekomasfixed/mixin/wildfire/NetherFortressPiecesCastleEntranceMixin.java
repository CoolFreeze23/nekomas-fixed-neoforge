package net.greenjab.nekomasfixed.mixin.wildfire;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Turns the fortress's open courtyard piece into the Wildfire's boss room. */
@Mixin(NetherFortressPieces.CastleEntrance.class)
public abstract class NetherFortressPiecesCastleEntranceMixin extends StructurePiece {
    @Unique
    private static final int X = 17;
    @Unique
    private static final int XC = (X-1)/2;
    @Unique
    private static final int Y = 15;

    protected NetherFortressPiecesCastleEntranceMixin(StructurePieceType type, int genDepth, BoundingBox box) {
        super(type, genDepth, box);
    }

    @WrapOperation(method = "createPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/BoundingBox;orientBox(IIIIIIIIILnet/minecraft/core/Direction;)Lnet/minecraft/world/level/levelgen/structure/BoundingBox;"))
    private static BoundingBox largerRoom(int x, int y, int z, int offsetX, int offsetY, int offsetZ, int width, int height, int depth, Direction orientation, Operation<BoundingBox> original) {
        return original.call(x, y, z, -(XC-1), offsetY, offsetZ, X, Y, X, orientation);
    }

    @ModifyConstant(method = "addChildren", constant = @Constant(intValue = 5))
    private int roomOffset(int constant) {
        return XC-1;
    }

    @Inject(method = "postProcess", at = @At(value = "HEAD"), cancellable = true)
    private void generateRoom(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos, CallbackInfo ci) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bricks = Blocks.NETHER_BRICKS.defaultBlockState();
        BlockState fenceEW = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
        BlockState fenceNS = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);


        //main box
        this.generateBox(level, chunkBB, 0, 2, 0, X-1, Y-2, X-1, bricks, bricks, false);
        this.generateBox(level, chunkBB, 2, 5, 2, X-3, Y-3, X-3, air, air, false);
        this.generateBox(level, chunkBB, 3, 4, 3, X-4, 4, X-4, air, air, false);

        //doorways
        this.generateBox(level, chunkBB, XC-1, 5, 0, XC+1, 8, 1, air, air, false);
        this.generateBox(level, chunkBB, XC-1, 5, X-2, XC+1, 8, X-1, air, air, false);
        this.generateBox(level, chunkBB, XC-1, 8, 0, XC+1, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false);
        this.generateBox(level, chunkBB, XC-2, 0, 0, XC+2, 1, 0, bricks, bricks, false);

        //roof
        int i;
        for (i = 1; i <= X-2; i += 2) {
            this.generateBox(level, chunkBB, i, 11, 0, i, Y-3, 0, fenceEW, fenceEW, false);
            this.generateBox(level, chunkBB, i, 11, X-1, i, Y-3, X-1, fenceEW, fenceEW, false);
            this.generateBox(level, chunkBB, 0, 11, i, 0, Y-3, i, fenceNS, fenceNS, false);
            this.generateBox(level, chunkBB, X-1, 11, i, X-1, Y-3, i, fenceNS, fenceNS, false);
            this.placeBlock(level, bricks, i, Y-1, 0, chunkBB);
            this.placeBlock(level, bricks, i, Y-1, X-1, chunkBB);
            this.placeBlock(level, bricks, 0, Y-1, i, chunkBB);
            this.placeBlock(level, bricks, X-1, Y-1, i, chunkBB);
            if (i != X-2) {
                this.placeBlock(level, fenceEW, i + 1, Y-1, 0, chunkBB);
                this.placeBlock(level, fenceEW, i + 1, Y-1, X-1, chunkBB);
                this.placeBlock(level, fenceNS, 0, Y-1, i + 1, chunkBB);
                this.placeBlock(level, fenceNS, X-1, Y-1, i + 1, chunkBB);
            }
        }

        this.placeBlock(level, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.EAST, true), 0, Y-1, 0, chunkBB);
        this.placeBlock(level, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.EAST, true), 0, Y-1, X-1, chunkBB);
        this.placeBlock(level, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true).setValue(FenceBlock.WEST, true), X-1, Y-1, X-1, chunkBB);
        this.placeBlock(level, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.WEST, true), X-1, Y-1, 0, chunkBB);

        //walls
        for(i = 3; i <= X-4; i += 2) {
            this.generateBox(level, chunkBB, 1, 7, i, 1, 10, i, fenceNS.setValue(FenceBlock.WEST, true), fenceNS.setValue(FenceBlock.WEST, true), false);
            this.generateBox(level, chunkBB, X-2, 7, i, X-2, 10, i, fenceNS.setValue(FenceBlock.EAST, true), fenceNS.setValue(FenceBlock.EAST, true), false);
            if (i != XC-1 && i != XC+1) {
                this.generateBox(level, chunkBB, i, 7, 1, i, 10, 1, fenceEW.setValue(FenceBlock.WEST, true), fenceNS.setValue(FenceBlock.WEST, true), false);
                this.generateBox(level, chunkBB, i, 7,X-2, i, 10, X-2, fenceEW.setValue(FenceBlock.EAST, true), fenceNS.setValue(FenceBlock.EAST, true), false);
            }
        }

        //floor
        this.placeBlock(level, Blocks.NETHERRACK.defaultBlockState(), 2, 4, 2, chunkBB);
        this.placeBlock(level, Blocks.NETHERRACK.defaultBlockState(), 2, 4, X-3, chunkBB);
        this.placeBlock(level, Blocks.NETHERRACK.defaultBlockState(), X-3, 4, 2, chunkBB);
        this.placeBlock(level, Blocks.NETHERRACK.defaultBlockState(), X-3, 4, X-3, chunkBB);

        this.placeBlock(level, Blocks.FIRE.defaultBlockState(), 2, 5, 2, chunkBB);
        this.placeBlock(level, Blocks.FIRE.defaultBlockState(), 2, 5, X-3, chunkBB);
        this.placeBlock(level, Blocks.FIRE.defaultBlockState(), X-3, 5, 2, chunkBB);
        this.placeBlock(level, Blocks.FIRE.defaultBlockState(), X-3, 5, X-3, chunkBB);

        this.placeBlock(level, bricks, 3, 4, 3, chunkBB);
        this.placeBlock(level, bricks, 3, 4, X-4, chunkBB);
        this.placeBlock(level, bricks, X-4, 4, 3, chunkBB);
        this.placeBlock(level, bricks, X-4, 4, X-4, chunkBB);

        for (int xx = 3; xx <= X-4; xx++) {
            for (int zz = 3; zz <= X-4; zz++) {
                if (level.getRandom().nextInt(3)==0) this.placeBlock(level, Blocks.MAGMA_BLOCK.defaultBlockState(), xx, 3, zz, chunkBB);
            }
        }

        for (int xx = XC-2; xx <= XC+2; xx++) {
            for (int zz = XC-2; zz <= XC+2; zz++) {
                this.placeBlock(level, (level.getRandom().nextInt(3)==0)?bricks:Blocks.MAGMA_BLOCK.defaultBlockState(), xx, 4, zz, chunkBB);
            }
        }
        this.placeBlock(level, air, XC-2, 4, XC-2, chunkBB);
        this.placeBlock(level, air, XC+2, 4, XC-2, chunkBB);
        this.placeBlock(level, air, XC-2, 4, XC+2, chunkBB);
        this.placeBlock(level, air, XC+2, 4, XC+2, chunkBB);

        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC, 4, XC, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC-1, 4, XC, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC+1, 4, XC, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC, 4, XC-1, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC, 4, XC+1, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC-1, 3, XC, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC+1, 3, XC, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC, 3, XC-1, chunkBB);
        this.placeBlock(level, Blocks.LAVA.defaultBlockState(), XC, 3, XC+1, chunkBB);

        BlockPos blockPos = this.getWorldPos( XC, 3, XC);
        if (chunkBB.isInside(blockPos)) {
            level.setBlock(blockPos, Blocks.TRIAL_SPAWNER.defaultBlockState(), 2);
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawnerBlockEntity) {
                CompoundTag nbt = new CompoundTag();
                nbt.putString("id", "minecraft:trial_spawner");
                nbt.put("normal_config", nekomasfixed$wildfireConfig(1.0F, "nekomasfixed:spawners/wildfire"));
                nbt.put("ominous_config", nekomasfixed$wildfireConfig(2.0F, "nekomasfixed:spawners/wildfire_ominous"));
                trialSpawnerBlockEntity.loadWithComponents(nbt, level.registryAccess());
            }
        }
        ci.cancel();
    }

    /** Trial spawner configs are written inline on 1.21.1, they aren't a data pack registry yet. */
    @Unique
    private static CompoundTag nekomasfixed$wildfireConfig(float mobs, String lootTable) {
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "nekomasfixed:wildfire");
        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entity);
        CompoundTag potential = new CompoundTag();
        potential.put("data", spawnData);
        potential.putInt("weight", 1);
        ListTag potentials = new ListTag();
        potentials.add(potential);

        CompoundTag loot = new CompoundTag();
        loot.putString("data", lootTable);
        loot.putInt("weight", 1);
        ListTag lootTables = new ListTag();
        lootTables.add(loot);

        CompoundTag config = new CompoundTag();
        config.put("loot_tables_to_eject", lootTables);
        config.putFloat("simultaneous_mobs", mobs);
        config.putFloat("simultaneous_mobs_added_per_player", 0.0F);
        config.put("spawn_potentials", potentials);
        config.putInt("ticks_between_spawn", 20);
        config.putFloat("total_mobs", mobs);
        config.putFloat("total_mobs_added_per_player", 0.0F);
        config.putInt("spawn_range", 1);
        return config;
    }
}
