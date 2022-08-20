package com.kneelawk.jarit.block

import com.kneelawk.jarit.Constants.id
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.EntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockView

object Blocks {
    val JAR by lazy {
        JarBlock(
            AbstractBlock.Settings.of(Material.GLASS)
                .strength(0.3F)
                .sounds(BlockSoundGroup.GLASS)
                .nonOpaque()
                .allowsSpawning(::never)
                .solidBlock(::never)
                .suffocates(::never)
                .blockVision(::never)
        )
    }
    val JAR_GLASS by lazy { Block(AbstractBlock.Settings.of(Material.GLASS)) }

    val JAR_INSIDE_GLASS by lazy {
        Block(
            AbstractBlock.Settings.of(Material.GLASS).strength(-1f, 3600000f).dropsNothing()
                .allowsSpawning(::never)
        )
    }

    fun init() {
        Registry.register(Registry.BLOCK, id("jar"), JAR)
        Registry.register(Registry.ITEM, id("jar"), BlockItem(JAR, Item.Settings()))
        Registry.register(Registry.BLOCK, id("jar_glass"), JAR_GLASS)
        Registry.register(Registry.ITEM, id("jar_glass"), BlockItem(JAR_GLASS, Item.Settings()))
        Registry.register(Registry.BLOCK, id("jar_inside_glass"), JAR_INSIDE_GLASS)
        Registry.register(Registry.ITEM, id("jar_inside_glass"), BlockItem(JAR_INSIDE_GLASS, Item.Settings()))
    }

    private fun never(p0: BlockState, p1: BlockView, p2: BlockPos, p3: EntityType<*>): Boolean = false
    private fun never(p0: BlockState, p1: BlockView, p2: BlockPos): Boolean = false
}
