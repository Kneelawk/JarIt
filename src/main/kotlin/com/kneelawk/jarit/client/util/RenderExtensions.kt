package com.kneelawk.jarit.client.util

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.math.Matrix4f

class SpriteRenderScope(private val sprite: Sprite) {
    fun VertexConsumer.frameUV(u: Int, v: Int): VertexConsumer =
        uv(sprite.getFrameU(u.toDouble()), sprite.getFrameV(v.toDouble()))
}

inline fun <R> Sprite.scope(f: SpriteRenderScope.() -> R): R {
    return SpriteRenderScope(this).f()
}

inline fun <R> SpriteIdentifier.scope(f: SpriteRenderScope.() -> R): R {
    return sprite.scope(f)
}

fun VertexConsumer.pixelVertex(matrix: Matrix4f, x: Int, y: Int, z: Int): VertexConsumer =
    vertex(matrix, x.toFloat() / 16f, y.toFloat() / 16f, z.toFloat() / 16f)
