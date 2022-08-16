package com.kneelawk.chunkinajar.dimension

import com.kneelawk.chunkinajar.Constants.MOD_ID
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
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

    val jars: Long2ObjectMap<JarInfo> = Long2ObjectLinkedOpenHashMap()

    constructor() : super() {
        maxJarSize = DEFAULT_MAX_JAR_SIZE
    }

    constructor(nbt: NbtCompound) : super() {
        maxJarSize = nbt.getInt("maxJarSize")

        val jarsList = nbt.getList("jars", NbtElement.COMPOUND_TYPE.toInt())
        for (elem in jarsList) {
            val jarTag = elem as NbtCompound
            val id = jarTag.getLong("id")
            val info = JarInfo.fromTag(jarTag)
            jars.put(id, info)
        }
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.putInt("maxJarSize", maxJarSize)

        val jarsList = NbtList()
        for ((id, info) in jars) {
            val jarTag = NbtCompound()
            jarTag.putLong("id", id)
            info.toTag(jarTag)
            jarsList.add(jarTag)
        }
        nbt.put("jars", jarsList)

        return nbt
    }
}
