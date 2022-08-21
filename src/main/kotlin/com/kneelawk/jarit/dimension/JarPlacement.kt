package com.kneelawk.jarit.dimension

import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.blockentity.JarBlockEntity
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.min
import net.minecraft.block.Blocks as MCBlocks

object JarPlacement {
    private const val REGION_SIZE = 32 * 16

    private val GLASS_STATE by lazy { Blocks.JAR_INSIDE_GLASS.defaultState }
    private val CORK_STATE by lazy { Blocks.JAR_INSIDE_CORK.defaultState }
    private val OUTSIZE_GLASS_STATE by lazy { Blocks.JAR_GLASS.defaultState }

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

    fun placeJar(world: World, id: Long, info: JarInfo, maxJarSize: Int) {
        val start = getJarStart(id, maxJarSize)
        val mutable = BlockPos.Mutable()

        // floor
        for (z in 0 until info.size + 2) {
            for (x in 0 until info.size + 2) {
                mutable.set(start, x, 0, z)
                world.setBlockState(mutable, GLASS_STATE)
            }
        }

        // -x wall
        for (y in 1 until info.size + 1) {
            for (z in 0 until info.size + 2) {
                mutable.set(start, 0, y, z)
                world.setBlockState(mutable, GLASS_STATE)
            }
        }

        // -z wall
        for (y in 1 until info.size + 1) {
            for (x in 1 until info.size + 1) {
                mutable.set(start, x, y, 0)
                world.setBlockState(mutable, GLASS_STATE)
            }
        }

        // +x wall
        for (y in 1 until info.size + 1) {
            for (z in 0 until info.size + 2) {
                mutable.set(start, info.size + 1, y, z)
                world.setBlockState(mutable, GLASS_STATE)
            }
        }

        // +z wall
        for (y in 1 until info.size + 1) {
            for (x in 1 until info.size + 1) {
                mutable.set(start, x, y, info.size + 1)
                world.setBlockState(mutable, GLASS_STATE)
            }
        }

        // cork
        for (z in 0 until info.size + 2) {
            for (x in 0 until info.size + 2) {
                mutable.set(start, x, info.size + 1, z)
                world.setBlockState(mutable, CORK_STATE)
            }
        }
    }

    fun placeOutsideJar(world: World, start: BlockPos, jarSize: Int) {
        val mutable = BlockPos.Mutable()

        // floor
        for (z in 0 until jarSize + 2) {
            for (x in 0 until jarSize + 2) {
                mutable.set(start, x, 0, z)
                world.setBlockState(mutable, OUTSIZE_GLASS_STATE)
            }
        }

        // -x wall
        for (y in 1 until jarSize + 1) {
            for (z in 0 until jarSize + 2) {
                mutable.set(start, 0, y, z)
                world.setBlockState(mutable, OUTSIZE_GLASS_STATE)
            }
        }

        // -z wall
        for (y in 1 until jarSize + 1) {
            for (x in 1 until jarSize + 1) {
                mutable.set(start, x, y, 0)
                world.setBlockState(mutable, OUTSIZE_GLASS_STATE)
            }
        }

        // +x wall
        for (y in 1 until jarSize + 1) {
            for (z in 0 until jarSize + 2) {
                mutable.set(start, jarSize + 1, y, z)
                world.setBlockState(mutable, OUTSIZE_GLASS_STATE)
            }
        }

        // +z wall
        for (y in 1 until jarSize + 1) {
            for (x in 1 until jarSize + 1) {
                mutable.set(start, x, y, jarSize + 1)
                world.setBlockState(mutable, OUTSIZE_GLASS_STATE)
            }
        }

        // top
        for (z in 0 until jarSize + 2) {
            for (x in 0 until jarSize + 2) {
                mutable.set(start, x, jarSize + 1, z)
                world.setBlockState(mutable, OUTSIZE_GLASS_STATE)
            }
        }
    }

    fun capture(world: ServerWorld, start: BlockPos, fullJarSize: Int) {
        val server = world.server

        val jarDim = server.getWorld(Dimensions.JAR_DIMENSION_WORLD_KEY) ?: run {
            System.err.println("Error getting jar dimension!")
            return
        }
        val jarDimInfo = JarDimensionInfo.get(jarDim)

        val jarSize = fullJarSize - 2
        val jarInfo = jarDimInfo.addJar(jarSize)
        val jarId = jarInfo.jarId

        val fromStart = start.add(BlockPos(1, 1, 1))

        placeJar(jarDim, jarId, jarInfo, jarDimInfo.maxJarSize)
        val toStart = getJarStart(jarId, jarDimInfo.maxJarSize).add(BlockPos(1, 1, 1))

        println("Placing new jar at: $toStart")

        val fromMut = fromStart.mutableCopy()
        val toMut = toStart.mutableCopy()
        for (y in (0 until jarSize).reversed()) {
            for (z in 0 until jarSize) {
                for (x in 0 until jarSize) {
                    fromMut.set(fromStart, x, y, z)
                    toMut.set(toStart, x, y, z)
                    jarDim.setBlockState(toMut, world.getBlockState(fromMut), Block.NOTIFY_LISTENERS)
                    world.setBlockState(fromMut, MCBlocks.AIR.defaultState, Block.NOTIFY_LISTENERS)
                }
            }
        }

        for (y in (0 until fullJarSize).reversed()) {
            for (z in 0 until fullJarSize) {
                for (x in 0 until fullJarSize) {
                    fromMut.set(start, x, y, z)
                    world.setBlockState(fromMut, MCBlocks.AIR.defaultState, Block.NOTIFY_LISTENERS)
                }
            }
        }

        val putJar = BlockPos(start.x + fullJarSize / 2, start.y + (fullJarSize - 1), start.z + fullJarSize / 2)

        world.setBlockState(putJar, Blocks.JAR.defaultState)
        (world.getBlockEntity(putJar) as JarBlockEntity).updateJarId(jarId)
    }

    fun release(world: ServerWorld, jar: BlockPos) {
        val jarBE = world.getBlockEntity(jar) as? JarBlockEntity ?: return
        val jarId = jarBE.jarId

        val server = world.server

        val jarDim = server.getWorld(Dimensions.JAR_DIMENSION_WORLD_KEY) ?: run {
            System.err.println("Error getting jar dimension!")
            return
        }
        val jarDimInfo = JarDimensionInfo.get(jarDim)
        val jarInfo = jarDimInfo.getJar(jarId)
        val jarSize = jarInfo.size
        val fullJarSize = jarSize + 2

        val dimStart = getJarStart(jarId, jarDimInfo.maxJarSize)
        val jarStart = BlockPos(jar.x - fullJarSize / 2, jar.y - (fullJarSize - 1), jar.z - fullJarSize / 2)

        val fromStart = dimStart.add(BlockPos(1, 1, 1))
        val toStart = jarStart.add(BlockPos(1, 1, 1))

        placeOutsideJar(world, jarStart, jarSize)

        val fromMut = fromStart.mutableCopy()
        val toMut = toStart.mutableCopy()
        for (y in (0 until jarSize).reversed()) {
            for (z in 0 until jarSize) {
                for (x in 0 until jarSize) {
                    fromMut.set(fromStart, x, y, z)
                    toMut.set(toStart, x, y, z)
                    world.setBlockState(toMut, jarDim.getBlockState(fromMut), Block.NOTIFY_LISTENERS)
                }
            }
        }

        for (y in (0 until jarDimInfo.maxJarSize + 2).reversed()) {
            for (z in 0 until jarDimInfo.maxJarSize + 2) {
                for (x in 0 until jarDimInfo.maxJarSize + 2) {
                    fromMut.set(dimStart, x, y, z)
                    jarDim.setBlockState(fromMut, MCBlocks.AIR.defaultState, Block.NOTIFY_LISTENERS)
                }
            }
        }

        jarDimInfo.removeJar(jarId)
    }
}
