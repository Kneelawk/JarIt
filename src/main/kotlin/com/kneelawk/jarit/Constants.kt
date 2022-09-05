package com.kneelawk.jarit

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object Constants {
    const val MOD_ID = "jar-it"

    fun id(path: String): Identifier = Identifier(MOD_ID, path)

    fun tt(prefix: String, path: String, vararg args: Any?): MutableText =
        Text.translatable("$prefix.$MOD_ID.$path", *args)

    fun msg(path: String, vararg args: Any?): MutableText = tt("message", path, *args)
}
