package com.kneelawk.jarit.item

import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.dimension.JarPlacement
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class JarCorkItem(settings: Settings) : Item(settings) {
    companion object {
        private fun isValid(world: World, pos: BlockPos): Boolean {
            if (world.getBlockState(pos).block != Blocks.JAR_GLASS) return false

            // currently only supports using a cork on the top of a jar
            if (world.getBlockState(pos.offset(Direction.DOWN)).block == Blocks.JAR_GLASS || world.getBlockState(
                    pos.offset(Direction.UP)
                ).block == Blocks.JAR_GLASS
            ) return false

            return true
        }

        private fun count(world: World, pos: BlockPos, dir: Direction): Int {
            val mut = pos.mutableCopy()
            var count = 0
            while (world.getBlockState(mut).block == Blocks.JAR_GLASS) {
                mut.move(dir)
                count++
            }
            return count
        }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val pos = context.blockPos

        if (!isValid(world, pos)) return ActionResult.FAIL

        val xpc = count(world, pos, Direction.EAST)
        val xnc = count(world, pos, Direction.WEST)
        val zpc = count(world, pos, Direction.SOUTH)
        val znc = count(world, pos, Direction.NORTH)

        val width = xpc + xnc - 1
        val depth = zpc + znc - 1

        if (width != depth) return ActionResult.FAIL

        val jarSize = width

        if (jarSize < 3) return ActionResult.FAIL

        val start = BlockPos(pos.subtract(BlockPos(xnc - 1, jarSize - 1, znc - 1)))

        if (world.getBlockState(start).block != Blocks.JAR_GLASS) return ActionResult.FAIL

        // FIXME: doesn't actually check that jar is complete

        if (world.isClient || world !is ServerWorld) return ActionResult.SUCCESS

        JarPlacement.capture(world, start, jarSize)

        return ActionResult.CONSUME
    }
}
