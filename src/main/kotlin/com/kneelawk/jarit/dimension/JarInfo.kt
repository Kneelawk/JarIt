package com.kneelawk.jarit.dimension

import net.minecraft.nbt.NbtCompound

data class JarInfo(val jarId: Long, val size: Int, val locked: Boolean) {
    companion object {
        fun fromTag(jarId: Long, nbt: NbtCompound): JarInfo =
            JarInfo(jarId, nbt.getInt("size"), nbt.getBoolean("locked"))
    }

    fun toTag(nbt: NbtCompound) {
        nbt.putInt("size", size)
        nbt.putBoolean("locked", locked)
    }
}
