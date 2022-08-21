package com.kneelawk.jarit.block

import com.kneelawk.jarit.Constants
import com.kneelawk.jarit.item.Items
import com.kneelawk.jarit.item.JarBlockItem
import com.kneelawk.jarit.util.multi
import net.minecraft.block.*
import net.minecraft.entity.EntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockView
import org.quiltmc.qkl.wrapper.minecraft.registry.registryScope

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
    val JAR_GLASS by lazy {
        GlassBlock(
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

    val JAR_INSIDE_GLASS by lazy {
        GlassBlock(
            AbstractBlock.Settings.of(Material.GLASS).strength(-1f, 3600000f).dropsNothing()
                .sounds(BlockSoundGroup.GLASS)
                .nonOpaque()
                .allowsSpawning(::never)
                .solidBlock(::never)
                .suffocates(::never)
                .blockVision(::never)
        )
    }

    val JAR_INSIDE_CORK by lazy {
        Block(
            AbstractBlock.Settings.of(Material.GLASS).strength(-1f, 3600000f).dropsNothing()
                .allowsSpawning(::never)
        )
    }

    fun init() {
        registryScope(Constants.MOD_ID) {
            multi(Registry.BLOCK, Registry.ITEM, ::simpleBlockItem) {
                JAR withId "jar" withAdded { JarBlockItem(it, Item.Settings()) }
                JAR_GLASS withId "jar_glass" withAdded ::tabBlockItem
                JAR_INSIDE_GLASS withId "jar_inside_glass"
                JAR_INSIDE_CORK withId "jar_inside_cork"
            }
        }
    }

    private fun never(p0: BlockState, p1: BlockView, p2: BlockPos, p3: EntityType<*>): Boolean = false
    private fun never(p0: BlockState, p1: BlockView, p2: BlockPos): Boolean = false

    private fun simpleBlockItem(block: Block): BlockItem = BlockItem(block, Item.Settings())
    private fun tabBlockItem(block: Block): BlockItem = BlockItem(block, Item.Settings().group(Items.ITEM_GROUP))
}
