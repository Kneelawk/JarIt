package com.kneelawk.jarit.blockentity

import com.kneelawk.jarit.Constants.id
import com.kneelawk.jarit.block.Blocks
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder

object BlockEntities {
    val JAR: BlockEntityType<JarBlockEntity> by lazy {
        QuiltBlockEntityTypeBuilder.create(::JarBlockEntity, Blocks.JAR).build()
    }

    fun init() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, id("jar"), JAR)
    }
}
