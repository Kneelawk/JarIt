package com.kneelawk.jarit.item

import com.kneelawk.jarit.Constants
import com.kneelawk.jarit.Constants.id
import com.kneelawk.jarit.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.registry.Registry
import org.quiltmc.qkl.wrapper.minecraft.registry.invoke
import org.quiltmc.qsl.item.group.api.QuiltItemGroup

object Items {
    val ITEM_GROUP: QuiltItemGroup by lazy {
        QuiltItemGroup.builder(id("item_group")).icon { ItemStack(Blocks.JAR) }.build()
    }
    val ITEM_SETTINGS: Item.Settings by lazy {
        Item.Settings().group(ITEM_GROUP)
    }

    val JAR_CORK by lazy {
        Item(ITEM_SETTINGS)
    }

    val JAR_OPENER by lazy {
        Item(ITEM_SETTINGS)
    }

    fun init() {
        Registry.ITEM(Constants.MOD_ID) {
            JAR_CORK withId "jar_cork"
            JAR_OPENER withId "jar_opener"
        }
    }
}
