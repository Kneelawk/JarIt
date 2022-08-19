package com.kneelawk.jarit.dimension

import com.kneelawk.jarit.Constants.id
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

object Dimensions {
    val JAR_DIMENSION_WORLD_KEY: RegistryKey<World> = RegistryKey.of(Registry.WORLD_KEY, id("jar-dimension"))

    fun init() {
        Registry.register(Registry.CHUNK_GENERATOR, id("void"), VoidChunkGenerator.CODEC)
    }
}
