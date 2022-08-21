package com.kneelawk.jarit.dimension

import net.minecraft.nbt.NbtCompound

data class JarInfo(val jarId: Long, val size: Int) {
    companion object {
        fun fromTag(jarId: Long, nbt: NbtCompound): JarInfo = JarInfo(jarId, nbt.getInt("size"))
    }

    fun toTag(nbt: NbtCompound) {
        nbt.putInt("size", size)
    }
}
