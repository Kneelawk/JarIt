package com.kneelawk.jarit.client

import com.kneelawk.jarit.blockentity.JarBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack

class JarBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) : BlockEntityRenderer<JarBlockEntity> {
    override fun render(
        entity: JarBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider,
        light: Int, overlay: Int
    ) {
    }
}
