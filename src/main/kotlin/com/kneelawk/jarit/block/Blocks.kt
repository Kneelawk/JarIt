package com.kneelawk.jarit.block

import com.kneelawk.jarit.Constants
import com.kneelawk.jarit.item.JarBlockItem
import com.kneelawk.jarit.util.multi
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
    val JAR_GLASS by lazy { Block(AbstractBlock.Settings.of(Material.GLASS)) }

    val JAR_INSIDE_GLASS by lazy {
        Block(
            AbstractBlock.Settings.of(Material.GLASS).strength(-1f, 3600000f).dropsNothing()
                .allowsSpawning(::never)
        )
    }

    fun init() {
        registryScope(Constants.MOD_ID) {
            multi(Registry.BLOCK, Registry.ITEM, { BlockItem(it, Item.Settings()) }) {
                JAR withId "jar" withAdded { JarBlockItem(it, Item.Settings()) }
                JAR_GLASS withId "jar_glass"
                JAR_INSIDE_GLASS withId "jar_inside_glass"
            }
        }
    }

    private fun never(p0: BlockState, p1: BlockView, p2: BlockPos, p3: EntityType<*>): Boolean = false
    private fun never(p0: BlockState, p1: BlockView, p2: BlockPos): Boolean = false
}
