package com.kneelawk.jarit.dimension

import net.minecraft.nbt.NbtCompound

data class JarInfo(val size: Int) {
    companion object {
        fun fromTag(nbt: NbtCompound): JarInfo = JarInfo(nbt.getInt("size"))
    }

    fun toTag(nbt: NbtCompound) {
        nbt.putInt("size", size)
    }
}
