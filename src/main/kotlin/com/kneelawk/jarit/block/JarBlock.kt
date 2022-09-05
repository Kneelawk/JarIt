package com.kneelawk.jarit.block

import com.kneelawk.jarit.blockentity.JarBlockEntity
import com.kneelawk.jarit.item.JarBlockItem
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class JarBlock(settings: Settings) : BlockWithEntity(settings) {
    companion object {
        private val OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(3.0 / 16.0, 0.0, 3.0 / 16.0, 13.0 / 16.0, 10.0 / 16.0, 13.0 / 16.0),
            VoxelShapes.cuboid(6.0 / 16.0, 10.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0, 11.0 / 16.0, 10.0 / 16.0),
            VoxelShapes.cuboid(5.0 / 16.0, 11.0 / 16.0, 5.0 / 16.0, 11.0 / 16.0, 12.0 / 16.0, 11.0 / 16.0)
        )
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = JarBlockEntity(pos, state)

    override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

    override fun getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos): Float = 1.0f

    override fun isTranslucent(state: BlockState, world: BlockView, pos: BlockPos): Boolean = true

    override fun getOutlineShape(
        state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext
    ): VoxelShape = OUTLINE

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        if (world.isClient || player.isCreative) return

        (world.getBlockEntity(pos) as? JarBlockEntity)?.let { jar ->
            val itemStack = ItemStack(Blocks.JAR)
            JarBlockItem.setJarId(itemStack, jar.jarId)

            val itemEntity =
                ItemEntity(world, pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5, itemStack)
            itemEntity.setToDefaultPickupDelay()
            world.spawnEntity(itemEntity)
        }
    }

    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        return (world.getBlockEntity(pos) as? JarBlockEntity)?.let { jar ->
            val itemStack = ItemStack(Blocks.JAR)
            JarBlockItem.setJarId(itemStack, jar.jarId)
            itemStack
        } ?: ItemStack(Blocks.JAR)
    }
}
