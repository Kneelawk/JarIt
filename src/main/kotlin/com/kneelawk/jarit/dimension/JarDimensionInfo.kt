package com.kneelawk.jarit.dimension

import com.kneelawk.jarit.Constants.MOD_ID
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class JarDimensionInfo : PersistentState {
    companion object {
        const val DEFAULT_MAX_JAR_SIZE = 62

        fun get(world: ServerWorld): JarDimensionInfo {
            if (world.registryKey != Dimensions.JAR_DIMENSION_WORLD_KEY) {
                throw IllegalArgumentException("JarDimensionInfo is only applicable to the jar dimension")
            }

            return world.persistentStateManager.getOrCreate(
                ::JarDimensionInfo, ::JarDimensionInfo, "${MOD_ID}_jar-dimension-data"
            )
        }
    }

    // allows me to change the max jar size later without messing up existing worlds
    val maxJarSize: Int

    private val jars: Long2ObjectMap<JarInfo> = Long2ObjectLinkedOpenHashMap()

    private var nextId: Long = 0

    constructor() : super() {
        maxJarSize = DEFAULT_MAX_JAR_SIZE
    }

    constructor(nbt: NbtCompound) : super() {
        maxJarSize = nbt.getInt("maxJarSize")

        val jarsList = nbt.getList("jars", NbtElement.COMPOUND_TYPE.toInt())
        for (elem in jarsList) {
            val jarTag = elem as NbtCompound
            val id = jarTag.getLong("id")
            val info = JarInfo.fromTag(id, jarTag)
            jars.put(id, info)
        }

        nextId = nbt.getLong("nextId")
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

        nbt.putLong("nextId", nextId)

        return nbt
    }

    fun addJar(jarSize: Int): JarInfo {
        val jarId = getNextJarId()
        val info = JarInfo(jarId, jarSize)
        jars.put(jarId, info)

        markDirty()
        return info
    }

    fun getJar(jarId: Long): JarInfo {
        return jars.get(jarId)
    }

    fun removeJar(jarId: Long) {
        jars.remove(jarId)
        markDirty()
    }

    private fun getNextJarId(): Long {
        var jarId = nextId++
        while (jars.containsKey(jarId)) {
            jarId = nextId++
        }

        markDirty()
        return jarId
    }
}
