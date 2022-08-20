package com.kneelawk.jarit

import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.blockentity.BlockEntities
import com.kneelawk.jarit.dimension.Dimensions
import org.quiltmc.loader.api.ModContainer

@Suppress("unused")
fun init(mod: ModContainer) {
    Blocks.init()
    BlockEntities.init()
    Dimensions.init()
}
