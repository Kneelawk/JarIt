package com.kneelawk.jarit.dimension

import com.kneelawk.jarit.Log
import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.blockentity.JarBlockEntity
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.*
import net.minecraft.world.TeleportTarget
import net.minecraft.world.World
import org.quiltmc.qkl.wrapper.minecraft.math.plus
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions
import java.util.function.Predicate
import java.util.stream.Stream
import kotlin.math.min
import net.minecraft.block.Blocks as MCBlocks

object JarPlacement {
    private const val REGION_SIZE = 32 * 16

    private val INSIDE_GLASS_STATE by lazy { Blocks.JAR_INSIDE_GLASS.defaultState }
    private val INSIDE_CORK_STATE by lazy { Blocks.JAR_INSIDE_CORK.defaultState }
    private val OUTSIDE_GLASS_STATE by lazy { Blocks.JAR_GLASS.defaultState }

    fun getJarStart(id: Long, maxSize: Int): BlockPos {
        val jarSize = maxSize + 2

        val maxYMultiple = (256 / jarSize).toLong()
        val y = (id % maxYMultiple).toInt() * jarSize
        val id1 = id / maxYMultiple
        val maxXMultiple = (REGION_SIZE / jarSize).toLong()
        val x = (id1 % maxXMultiple).toInt() * jarSize
        val id2 = id1 / maxXMultiple
        val maxZMultiple = (REGION_SIZE / jarSize).toLong()
        val z = (id2 % maxZMultiple).toInt() * jarSize

        var i = id2 / maxZMultiple
        var layer = 1
        var multiple = 1
        var vertical = false
        var regionX = 0
        var regionZ = 0
        while (i > 0) {
            val shift = min(layer.toLong(), i).toInt()

            if (vertical) {
                regionZ += shift * multiple

                multiple = -multiple
                layer++
            } else {
                regionX += shift * multiple
            }

            vertical = !vertical
            i -= shift
        }

        return BlockPos(x + regionX * REGION_SIZE, y, z + regionZ * REGION_SIZE)
    }

    fun getJarChunks(id: Long, jarSize: Int, maxSize: Int): Stream<ChunkPos> {
        val startPos = getJarStart(id, maxSize)
        val endPos = startPos + BlockPos(jarSize + 2, 0, jarSize + 2)

        return ChunkPos.stream(ChunkPos(startPos), ChunkPos(endPos))
    }

    fun getJarDimension(server: MinecraftServer): ServerWorld? {
        return server.getWorld(Dimensions.JAR_DIMENSION_WORLD_KEY) ?: run {
            Log.log.error("Error getting jar dimension!")
            return null
        }
    }

    fun createNewJar(server: MinecraftServer, fullJarSize: Int): JarCreateResult {
        val jarDim = getJarDimension(server) ?: return JarCreateResult.NoJarDimension
        val jarDimInfo = JarDimensionInfo.get(jarDim)

        val jarSize = fullJarSize - 2
        val jarInfo = jarDimInfo.addJar(jarSize)
        val jarId = jarInfo.jarId

        val jarStart = getJarStart(jarId, jarDimInfo.maxJarSize) + BlockPos(1, 1, 1)
        Log.log.info("Creating new jar at: $jarStart")

        placeJar(jarDim, jarId, jarInfo, jarDimInfo.maxJarSize)

        return JarCreateResult.Success(jarInfo)
    }

    fun createJarWithId(server: MinecraftServer, fullJarSize: Int, jarId: Long): JarCreateResult {
        val jarDim = getJarDimension(server) ?: return JarCreateResult.NoJarDimension
        val jarDimInfo = JarDimensionInfo.get(jarDim)

        if (jarDimInfo.hasJar(jarId)) return JarCreateResult.JarAlreadyExists

        val jarSize = fullJarSize - 2
        val jarInfo = JarInfo(jarId, jarSize)
        jarDimInfo.putJar(jarInfo)

        val jarStart = getJarStart(jarId, jarDimInfo.maxJarSize) + BlockPos(1, 1, 1)
        Log.log.info("Creating new jar at: $jarStart")

        placeJar(jarDim, jarId, jarInfo, jarDimInfo.maxJarSize)

        return JarCreateResult.Success(jarInfo)
    }

    fun destroyJar(server: MinecraftServer, jarDim: ServerWorld, jarDimInfo: JarDimensionInfo, jarInfo: JarInfo) {
        val overworld = server.overworld
        val spawn = Vec3d.ofBottomCenter(overworld.spawnPos)

        val dimStart = getJarStart(jarInfo.jarId, jarDimInfo.maxJarSize)
        val fromStart = dimStart + BlockPos(1, 1, 1)

        val insideArea = Box(fromStart, fromStart.add(BlockPos(jarInfo.size, jarInfo.size, jarInfo.size)))

        val entities = jarDim.getOtherEntities(null, insideArea)
        entities.forEach {
            QuiltDimensions.teleport<Entity>(it, overworld, TeleportTarget(spawn, it.velocity, it.headYaw, it.pitch))
        }

        clearArea(jarDim, dimStart, jarDimInfo.maxJarSize + 2, true)

        jarDimInfo.removeJar(jarInfo.jarId)
    }

    fun capture(world: ServerWorld, jarStart: BlockPos, fullJarSize: Int) {
        val jarDim = getJarDimension(world.server) ?: return
        val jarDimInfo = JarDimensionInfo.get(jarDim)

        val jarSize = fullJarSize - 2
        val jarInfo = jarDimInfo.addJar(jarSize)
        val jarId = jarInfo.jarId

        val dimStart = getJarStart(jarId, jarDimInfo.maxJarSize)

        val fromStart = jarStart.add(BlockPos(1, 1, 1))
        val toStart = dimStart.add(BlockPos(1, 1, 1))

        Log.log.info("Placing new jar at: $toStart")

        placeJar(jarDim, jarId, jarInfo, jarDimInfo.maxJarSize)

        copyContents(fromStart, toStart, world, jarDim, jarSize)

        moveEntities(fromStart, toStart, world, jarDim, jarSize)

        clearArea(world, jarStart, fullJarSize, true)

        val putJar = BlockPos(jarStart.x + fullJarSize / 2, jarStart.y, jarStart.z + fullJarSize / 2)

        world.setBlockState(putJar, Blocks.JAR.defaultState)
        (world.getBlockEntity(putJar) as JarBlockEntity).updateJarId(jarId)
    }

    fun release(world: ServerWorld, jar: BlockPos) {
        val jarBE = world.getBlockEntity(jar) as? JarBlockEntity ?: return
        val jarId = jarBE.jarId

        val jarDim = getJarDimension(world.server) ?: return
        val jarDimInfo = JarDimensionInfo.get(jarDim)
        val jarInfo = jarDimInfo.getJar(jarId)

        if (jarInfo == null) {
            world.breakBlock(jar, false)
            return
        }

        val jarSize = jarInfo.size
        val fullJarSize = jarSize + 2

        val dimStart = getJarStart(jarId, jarDimInfo.maxJarSize)
        val jarStart = BlockPos(jar.x - fullJarSize / 2, jar.y, jar.z - fullJarSize / 2)

        val fromStart = dimStart.add(BlockPos(1, 1, 1))
        val toStart = jarStart.add(BlockPos(1, 1, 1))

        // clear out blocks in the way
        clearArea(world, jarStart, fullJarSize, false)

        placeOutsideJar(world, jarStart, jarSize)

        copyContents(fromStart, toStart, jarDim, world, jarSize)

        moveEntities(fromStart, toStart, jarDim, world, jarSize)

        clearArea(jarDim, dimStart, jarDimInfo.maxJarSize + 2, true)

        jarDimInfo.removeJar(jarId)
    }

    fun findSafeDestination(server: MinecraftServer, jarInfo: JarInfo): BlockPos? {
        val jarDim = getJarDimension(server) ?: return null
        val jarDimInfo = JarDimensionInfo.get(jarDim)
        return findSafeDestination(jarDim, jarInfo, jarDimInfo.maxJarSize)
    }

    fun findSafeDestination(jarDim: World, jarInfo: JarInfo, maxJarSize: Int): BlockPos? {
        val jarStart = getJarStart(jarInfo.jarId, maxJarSize) + BlockPos(1, 1, 1)

        fun airLike(pos: BlockPos): Predicate<BlockState> {
            return Predicate { it.getCollisionShape(jarDim, pos).isEmpty }
        }

        val mut = BlockPos.Mutable()
        for (y in 0 until jarInfo.size) {
            for (z in 0 until jarInfo.size) {
                for (x in 0 until jarInfo.size) {
                    mut.set(jarStart, x, y, z)

                    if (jarDim.testBlockState(mut, airLike(mut))) {
                        val up = mut.offset(Direction.UP)
                        if (jarDim.testBlockState(up, airLike(up))) {
                            return mut.toImmutable()
                        }
                    }
                }
            }
        }

        return null
    }

    private fun placeJar(world: World, id: Long, info: JarInfo, maxJarSize: Int) {
        placeJar(world, getJarStart(id, maxJarSize), info.size, INSIDE_GLASS_STATE, INSIDE_CORK_STATE)
    }

    private fun placeOutsideJar(world: World, start: BlockPos, jarSize: Int) {
        placeJar(world, start, jarSize, OUTSIDE_GLASS_STATE, OUTSIDE_GLASS_STATE)
    }

    private fun placeJar(world: World, start: BlockPos, jarSize: Int, glassState: BlockState, corkState: BlockState) {
        val mutable = BlockPos.Mutable()

        // floor
        for (z in 0 until jarSize + 2) {
            for (x in 0 until jarSize + 2) {
                mutable.set(start, x, 0, z)
                world.setBlockState(mutable, glassState)
            }
        }

        // -x wall
        for (y in 0 until jarSize + 2) {
            for (z in 0 until jarSize + 2) {
                mutable.set(start, 0, y, z)
                world.setBlockState(mutable, glassState)
            }
        }

        // -z wall
        for (y in 0 until jarSize + 2) {
            for (x in 0 until jarSize + 2) {
                mutable.set(start, x, y, 0)
                world.setBlockState(mutable, glassState)
            }
        }

        // +x wall
        for (y in 0 until jarSize + 2) {
            for (z in 0 until jarSize + 2) {
                mutable.set(start, jarSize + 1, y, z)
                world.setBlockState(mutable, glassState)
            }
        }

        // +z wall
        for (y in 0 until jarSize + 2) {
            for (x in 0 until jarSize + 2) {
                mutable.set(start, x, y, jarSize + 1)
                world.setBlockState(mutable, glassState)
            }
        }

        // top
        for (z in 0 until jarSize + 2) {
            for (x in 0 until jarSize + 2) {
                mutable.set(start, x, jarSize + 1, z)
                world.setBlockState(mutable, corkState)
            }
        }
    }

    private fun copyContents(
        fromStart: BlockPos, toStart: BlockPos, fromWorld: ServerWorld, toWorld: ServerWorld, jarSize: Int
    ) {
        val fromMut = BlockPos.Mutable()
        val toMut = BlockPos.Mutable()
        for (y in 0 until jarSize) {
            for (z in 0 until jarSize) {
                for (x in 0 until jarSize) {
                    fromMut.set(fromStart, x, y, z)
                    toMut.set(toStart, x, y, z)
                    toWorld.setBlockState(
                        toMut, fromWorld.getBlockState(fromMut), Block.NOTIFY_LISTENERS or Block.FORCE_STATE
                    )

                    fromWorld.getBlockEntity(fromMut)?.let { oldBE ->
                        val tag = oldBE.toNbt()
                        modifyNBT(tag, toMut)
                        toWorld.getBlockEntity(toMut)?.readNbt(tag)
                    }
                }
            }
        }
    }

    private fun modifyNBT(tag: NbtCompound, toMut: BlockPos) {
        if (tag.contains("x", NbtElement.NUMBER_TYPE.toInt())) tag.putInt("x", toMut.x)
        if (tag.contains("y", NbtElement.NUMBER_TYPE.toInt())) tag.putInt("y", toMut.y)
        if (tag.contains("z", NbtElement.NUMBER_TYPE.toInt())) tag.putInt("z", toMut.z)
    }

    private fun moveEntities(
        fromStart: BlockPos, toStart: BlockPos, fromWorld: ServerWorld, toWorld: ServerWorld, jarSize: Int
    ) {
        val insideArea = Box(fromStart, fromStart.add(BlockPos(jarSize, jarSize, jarSize)))

        val offset = Vec3d.of(toStart.subtract(fromStart))

        val entities = fromWorld.getOtherEntities(null, insideArea)
        entities.forEach {
            val oldPos = it.pos
            val newPos = oldPos.add(offset)
            QuiltDimensions.teleport<Entity>(it, toWorld, TeleportTarget(newPos, it.velocity, it.headYaw, it.pitch))
        }
    }

    private fun clearArea(world: ServerWorld, start: BlockPos, fullJarSize: Int, destroy: Boolean) {
        val toDrop = ObjectArrayList<Pair<ItemStack, BlockPos>>()

        val mut = BlockPos.Mutable()
        for (y in (0 until fullJarSize).reversed()) {
            for (z in 0 until fullJarSize) {
                for (x in 0 until fullJarSize) {
                    mut.set(start, x, y, z)

                    if (destroy) {
                        world.removeBlockEntity(mut)
                        world.setBlockState(mut, MCBlocks.AIR.defaultState, Block.NOTIFY_LISTENERS or Block.SKIP_DROPS)
                    } else {
                        val state = world.getBlockState(mut)
                        if (!state.isAir) {
                            val imm = mut.toImmutable()
                            val blockEntity = if (state.hasBlockEntity()) world.getBlockEntity(imm) else null

                            val builder = LootContext.Builder(world)
                                .random(world.random)
                                .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(imm))
                                .parameter(LootContextParameters.TOOL, ItemStack.EMPTY)
                                .optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity)

                            state.onStacksDropped(world, imm, ItemStack.EMPTY, false)
                            state.getDroppedStacks(builder).forEach { tryMergeStack(toDrop, it, imm) }

                            world.setBlockState(imm, MCBlocks.AIR.defaultState, Block.NOTIFY_ALL)
                        }
                    }
                }
            }
        }

        for ((stack, pos) in toDrop) {
            Block.dropStack(world, pos, stack)
        }
    }

    private fun tryMergeStack(stacks: ObjectArrayList<Pair<ItemStack, BlockPos>>, stack: ItemStack, pos: BlockPos) {
        val i = stacks.size
        for (j in 0 until i) {
            val pair = stacks[j]
            val itemStack = pair.first
            if (ItemEntity.canMerge(itemStack, stack)) {
                val itemStack2 = ItemEntity.merge(itemStack, stack, 16)
                stacks[j] = Pair(itemStack2, pair.second)
                if (stack.isEmpty) {
                    return
                }
            }
        }
        stacks.add(Pair(stack, pos))
    }

    sealed interface JarCreateResult {
        object NoJarDimension : JarCreateResult
        object JarAlreadyExists : JarCreateResult
        data class Success(val info: JarInfo) : JarCreateResult
    }
}
