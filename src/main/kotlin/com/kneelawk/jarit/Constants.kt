package com.kneelawk.jarit

import net.minecraft.util.Identifier

object Constants {
    const val MOD_ID = "jar-it"

    fun id(path: String): Identifier = Identifier(MOD_ID, path)
}
