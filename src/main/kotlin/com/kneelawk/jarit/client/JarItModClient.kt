package com.kneelawk.jarit.client

import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.blockentity.BlockEntities
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.client.render.RenderLayer
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap

@Suppress("unused")
fun init() {
    JarBlockEntityRenderer.init()
    BlockEntityRendererRegistry.register(BlockEntities.JAR, ::JarBlockEntityRenderer)

    BlockRenderLayerMap.put(RenderLayer.getCutout(), Blocks.JAR)
    BlockRenderLayerMap.put(RenderLayer.getCutout(), Blocks.JAR_GLASS)
    BlockRenderLayerMap.put(RenderLayer.getCutout(), Blocks.JAR_INSIDE_GLASS)
}
