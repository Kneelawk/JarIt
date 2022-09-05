package com.kneelawk.jarit.dimension

import com.kneelawk.jarit.Constants.MOD_ID
import com.kneelawk.jarit.Log
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2LongMap
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.PersistentState
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents

class JarDimensionInfo : PersistentState {
    companion object {
        const val DEFAULT_MAX_JAR_SIZE = 62
        const val MAX_JAR_AGE = 300

        fun init() {
            ServerTickEvents.END.register { get(it)?.tick() }
        }

        fun get(server: MinecraftServer): JarDimensionInfo? {
            val jarDim = server.getWorld(Dimensions.JAR_DIMENSION_WORLD_KEY)

            if (jarDim == null) {
                Log.log.error("Error getting jar dimension!")
                return null
            }

            return get(jarDim)
        }

        fun get(world: ServerWorld): JarDimensionInfo {
            if (world.registryKey != Dimensions.JAR_DIMENSION_WORLD_KEY) {
                throw IllegalArgumentException("JarDimensionInfo is only applicable to the jar dimension")
            }

            return world.persistentStateManager.getOrCreate(
                { JarDimensionInfo(world, it) }, { JarDimensionInfo(world) }, "${MOD_ID}_jar-dimension-data"
            )
        }
    }

    val world: ServerWorld

    // allows me to change the max jar size later without messing up existing worlds
    val maxJarSize: Int

    private val jars: Long2ObjectMap<JarInfo> = Long2ObjectLinkedOpenHashMap()
    private val loadedChunks: Long2LongMap = Long2LongLinkedOpenHashMap()

    private var nextId: Long = 0

    constructor(world: ServerWorld) : super() {
        this.world = world
        maxJarSize = DEFAULT_MAX_JAR_SIZE
    }

    constructor(world: ServerWorld, nbt: NbtCompound) : super() {
        this.world = world

        maxJarSize = nbt.getInt("maxJarSize")

        val jarsList = nbt.getList("jars", NbtElement.COMPOUND_TYPE.toInt())
        for (elem in jarsList) {
            val jarTag = elem as NbtCompound
            val id = jarTag.getLong("id")
            val info = JarInfo.fromTag(id, jarTag)
            jars.put(id, info)
        }

        nextId = nbt.getLong("nextId")

        val loadedChunksList = nbt.getList("loadedChunks", NbtElement.COMPOUND_TYPE.toInt())
        for (elem in loadedChunksList) {
            val chunkTag = elem as NbtCompound
            val posLong = ChunkPos.toLong(chunkTag.getInt("x"), chunkTag.getInt("z"))
            val lastTick = chunkTag.getLong("lastTick")
            loadedChunks.put(posLong, lastTick)
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

        nbt.putLong("nextId", nextId)

        val loadedChunksList = NbtList()
        for ((posLong, lastTick) in loadedChunks) {
            val chunkTag = NbtCompound()
            val pos = ChunkPos(posLong)
            chunkTag.putInt("x", pos.x)
            chunkTag.putInt("z", pos.z)
            chunkTag.putLong("lastTick", lastTick)
            loadedChunksList.add(chunkTag)
        }
        nbt.put("loadedChunks", loadedChunksList)

        return nbt
    }

    private fun tick() {
        val curTick = world.time
        val it = loadedChunks.keys.longIterator()
        while (it.hasNext()) {
            val chunkLong = it.nextLong()
            val lastTick = loadedChunks.get(chunkLong)

            if (lastTick + MAX_JAR_AGE < curTick) {
                it.remove()

                val chunkPos = ChunkPos(chunkLong)
                world.setChunkForced(chunkPos.x, chunkPos.z, false)
            }
        }
    }

    fun tickJar(jarId: Long) {
        val info = jars[jarId] ?: return

        JarPlacement.getJarChunks(jarId, info.size, maxJarSize).forEach {
            val key = it.toLong()
            if (!loadedChunks.containsKey(key)) {
                world.setChunkForced(it.x, it.z, true)
            }
            loadedChunks.put(key, world.time)
        }
    }

    fun addJar(jarSize: Int): JarInfo {
        val jarId = getNextJarId()
        val info = JarInfo(jarId, jarSize)
        jars.put(jarId, info)

        markDirty()
        return info
    }

    fun getJar(jarId: Long): JarInfo? {
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
