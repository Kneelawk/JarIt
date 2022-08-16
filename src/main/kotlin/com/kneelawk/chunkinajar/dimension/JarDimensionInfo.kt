package com.kneelawk.chunkinajar.dimension

import com.kneelawk.chunkinajar.Constants.MOD_ID
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class JarDimensionInfo : PersistentState {
    companion object {
        // means a jar can be stored in a single 16x16x16 space in the jar dimension
        const val DEFAULT_MAX_JAR_SIZE = 14

        fun get(world: ServerWorld) {
            if (world.registryKey != Dimensions.JAR_DIMENSION_WORLD_KEY) {
                throw IllegalArgumentException("JarDimensionInfo is only applicable to the jar dimension")
            }

            world.persistentStateManager.getOrCreate(
                ::JarDimensionInfo, ::JarDimensionInfo, "${MOD_ID}_jar-dimension-data"
            )
        }
    }

    // allows me to change the max jar size later without messing up existing worlds
    val maxJarSize: Int

    val takenIds: LongSet = LongLinkedOpenHashSet()

    constructor() : super() {
        maxJarSize = DEFAULT_MAX_JAR_SIZE
    }

    constructor(nbt: NbtCompound) : super() {
        maxJarSize = nbt.getInt("maxJarSize")

        val idList = nbt.getLongArray("takenIds")
        for (id in idList) {
            takenIds.add(id)
        }
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.putInt("maxJarSize", maxJarSize)

        val idList = takenIds.toLongArray()
        nbt.putLongArray("takenIds", idList)

        return nbt
    }
}
