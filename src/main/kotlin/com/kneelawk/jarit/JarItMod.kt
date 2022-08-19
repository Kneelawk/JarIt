package com.kneelawk.jarit

import com.kneelawk.jarit.dimension.Dimensions
import org.quiltmc.loader.api.ModContainer

@Suppress("unused")
fun init(mod: ModContainer) {
    Dimensions.init()
}
