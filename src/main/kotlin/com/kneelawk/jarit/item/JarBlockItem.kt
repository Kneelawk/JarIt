package com.kneelawk.jarit.item

import com.kneelawk.jarit.Constants
import com.kneelawk.jarit.blockentity.JarBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class JarBlockItem(block: Block, settings: Settings) : BlockItem(block, settings) {
    companion object {
        fun getJarId(stack: ItemStack): Long? {
            val nbt = stack.getSubNbt(Constants.MOD_ID) ?: return null

            return if (nbt.contains("jarId", NbtElement.NUMBER_TYPE.toInt())) {
                nbt.getLong("jarId")
            } else {
                null
            }
        }

        fun setJarId(stack: ItemStack, jarId: Long) {
            val nbt = stack.getSubNbt(Constants.MOD_ID) ?: NbtCompound()
            nbt.putLong("jarId", jarId)
            stack.setSubNbt(Constants.MOD_ID, nbt)
        }
    }

    override fun postPlacement(
        pos: BlockPos, world: World, player: PlayerEntity?, stack: ItemStack, state: BlockState
    ): Boolean {
        return if (world.isClient) {
            false
        } else {
            val jarId = getJarId(stack) ?: return false

            (world.getBlockEntity(pos) as? JarBlockEntity)?.updateJarId(jarId)

            true
        }
    }

    override fun appendTooltip(
        stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext
    ) {
        super.appendTooltip(stack, world, tooltip, context)

        val jarId = getJarId(stack)

        if (jarId != null) {
            tooltip.add(Text.literal("Jar Id: $jarId"))
        } else {
            tooltip.add(Text.literal("Invalid!"))
        }
    }
}
