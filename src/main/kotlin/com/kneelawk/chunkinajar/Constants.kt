package com.kneelawk.chunkinajar

import net.minecraft.util.Identifier

object Constants {
    const val MOD_ID = "chunk-in-a-jar"

    fun id(path: String): Identifier = Identifier(MOD_ID, path)
}
