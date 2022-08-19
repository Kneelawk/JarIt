package com.kneelawk.jarit.dimension

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.structure.StructureManager
import net.minecraft.util.HolderSet
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.ChunkRegion
import net.minecraft.world.HeightLimitView
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.GenerationSettings
import net.minecraft.world.biome.source.BiomeAccess
import net.minecraft.world.biome.source.BiomeSource
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.RandomState
import net.minecraft.world.gen.chunk.Blender
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.chunk.VerticalBlockSample
import net.minecraft.world.gen.structure.StructureSet
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class VoidChunkGenerator(structureRegistry: Registry<StructureSet>, biomeSource: BiomeSource) : ChunkGenerator(
    structureRegistry, Optional.of(HolderSet.createDirect()), biomeSource, { GenerationSettings.INSTANCE }) {
    companion object {
        val CODEC: Codec<VoidChunkGenerator> = RecordCodecBuilder.create { instance ->
            return@create method_41042(instance).and(
                BiomeSource.CODEC.fieldOf("biome_source").forGetter(VoidChunkGenerator::getBiomeSource)
            ).apply(instance, ::VoidChunkGenerator)
        }
    }

    override fun getCodec(): Codec<out ChunkGenerator> = CODEC

    override fun carve(
        chunkRegion: ChunkRegion, seed: Long, randomState: RandomState, biomeAccess: BiomeAccess,
        structureManager: StructureManager, chunk: Chunk, generationStep: GenerationStep.Carver
    ) = Unit

    override fun buildSurface(
        region: ChunkRegion, structureManager: StructureManager, randomState: RandomState, chunk: Chunk
    ) = Unit

    override fun populateEntities(region: ChunkRegion) = Unit

    // FIXME: hardcoded world height
    override fun getWorldHeight(): Int = 256

    override fun populateNoise(
        executor: Executor, blender: Blender, randomState: RandomState, structureManager: StructureManager, chunk: Chunk
    ): CompletableFuture<Chunk> = CompletableFuture.completedFuture(chunk)

    override fun getSeaLevel(): Int = -63

    override fun getMinimumY(): Int = 0

    override fun getHeight(
        x: Int, z: Int, heightmap: Heightmap.Type, world: HeightLimitView, randomState: RandomState
    ): Int = world.bottomY

    override fun getColumnSample(
        x: Int, z: Int, world: HeightLimitView, randomState: RandomState
    ): VerticalBlockSample = VerticalBlockSample(world.bottomY, emptyArray())

    override fun m_hfetlfug(list: MutableList<String>, randomState: RandomState, pos: BlockPos) = Unit
}
