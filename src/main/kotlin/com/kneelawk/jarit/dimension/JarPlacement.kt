package com.kneelawk.jarit.dimension

import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.min

object JarPlacement {
    private const val REGION_SIZE = 32 * 16

    private val GLASS_STATE by lazy { Blocks.GLASS.defaultState }
    private val CORK_STATE by lazy { Blocks.OAK_PLANKS.defaultState }

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
                mutable.set(start, info.size + 2, y, z)
                world.setBlockState(mutable, GLASS_STATE)
            }
        }

        // +z wall
        for (y in 1 until info.size + 1) {
            for (x in 1 until info.size + 1) {
                mutable.set(start, x, y, info.size + 2)
                world.setBlockState(mutable, GLASS_STATE)
            }
        }

        // cork
        for (z in 0 until info.size + 2) {
            for (x in 0 until info.size + 2) {
                mutable.set(start, x, info.size + 2, z)
                world.setBlockState(mutable, CORK_STATE)
            }
        }
    }
}
