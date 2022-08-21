package com.kneelawk.jarit

import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.blockentity.BlockEntities
import com.kneelawk.jarit.dimension.Dimensions
import com.kneelawk.jarit.item.Items
import com.kneelawk.jarit.net.Networking
import org.quiltmc.loader.api.ModContainer

@Suppress("unused")
fun init(mod: ModContainer) {
    Blocks.init()
    Items.init()
    BlockEntities.init()
    Dimensions.init()
    Networking.init()
}
