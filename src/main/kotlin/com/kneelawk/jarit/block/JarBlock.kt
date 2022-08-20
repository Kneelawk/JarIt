package com.kneelawk.jarit.block

import com.kneelawk.jarit.blockentity.JarBlockEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

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
}
