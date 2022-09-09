package com.kneelawk.jarit.item

import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.dimension.JarPlacement
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult

class JarOpenerItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val pos = context.blockPos

        if (world.getBlockState(pos).block != Blocks.JAR) return ActionResult.FAIL

        if (world.isClient || world !is ServerWorld) return ActionResult.SUCCESS

        JarPlacement.release(world, pos, context.player)

        return ActionResult.CONSUME
    }
}
