package com.kneelawk.jarit.blockentity

import com.kneelawk.jarit.dimension.JarDimensionInfo
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class JarBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BlockEntities.JAR, pos, state) {
    companion object {
        fun tick(world: World, blockPos: BlockPos, blockState: BlockState, blockEntity: JarBlockEntity) {
            if (world !is ServerWorld) return

            val dimInfo = JarDimensionInfo.get(world.server) ?: return

            dimInfo.tickJar(blockEntity.jarId)
        }
    }

    var jarId: Long = 0
        private set

    fun updateJarId(newJarId: Long) {
        jarId = newJarId
        markDirty()
    }

    override fun readNbt(nbt: NbtCompound) {
        jarId = nbt.getLong("jarId")
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putLong("jarId", jarId)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return NbtCompound().also(::writeNbt)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.of(this)
    }
}
