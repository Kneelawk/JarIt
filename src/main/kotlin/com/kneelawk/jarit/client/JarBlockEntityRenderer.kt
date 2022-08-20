package com.kneelawk.jarit.client

import com.kneelawk.jarit.Constants.id
import com.kneelawk.jarit.blockentity.JarBlockEntity
import com.kneelawk.jarit.client.util.pixelVertex
import com.kneelawk.jarit.client.util.scope
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler

class JarBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) : BlockEntityRenderer<JarBlockEntity> {
    companion object {
        private val bottom = id("block/jar_bottom_inside")
        private val side = id("block/jar_side_inside")
        private val bottomId = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, bottom)
        private val sideId = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, bottom)

        fun init() {
            ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register { _, registry ->
                registry.register(bottom)
                registry.register(side)
            }
        }
    }

    override fun render(
        entity: JarBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider,
        light: Int, overlay: Int
    ) {
        val model = matrices.peek().position
        val normal = matrices.peek().normal
        val buf = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))

        bottomId.scope {
            buf.pixelVertex(model, 4.0, 1.0, 4.0).color(-1).frameUV(4.0, 4.0).overlay(overlay)
                .light(light).normal(normal, 0f, 1f, 0f).next()
            buf.pixelVertex(model, 4.0, 1.0, 12.0).color(-1).frameUV(4.0, 12.0).overlay(overlay)
                .light(light).normal(normal, 0f, 1f, 0f).next()
            buf.pixelVertex(model, 12.0, 1.0, 12.0).color(-1).frameUV(12.0, 12.0).overlay(overlay)
                .light(light).normal(normal, 0f, 1f, 0f).next()
            buf.pixelVertex(model, 12.0, 1.0, 4.0).color(-1).frameUV(12.0, 4.0).overlay(overlay)
                .light(light).normal(normal, 0f, 1f, 0f).next()
        }
    }
}
