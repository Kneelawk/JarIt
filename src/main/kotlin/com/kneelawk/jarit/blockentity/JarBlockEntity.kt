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
        private const val TICK_DELAY = 20

        fun tick(world: World, blockPos: BlockPos, blockState: BlockState, blockEntity: JarBlockEntity) {
            if (world !is ServerWorld) return

            val time = world.server.timeReference
            if (blockEntity.lastTick + TICK_DELAY < time) {
                // check whether we should remove ourselves
                val dimInfo = JarDimensionInfo.get(world.server) ?: return
                val jarId = blockEntity.jarId
                if (!dimInfo.hasJar(jarId)) {
                    world.breakBlock(blockPos, false)
                    return
                }

                // we're still valid, so let's make sure things keep running
                blockEntity.lastTick = time
                blockEntity.markDirty()

                dimInfo.tickJar(jarId)
            }
        }
    }

    var jarId: Long = 0
        private set
    private var lastTick: Long = 0

    fun updateJarId(newJarId: Long) {
        jarId = newJarId
        markDirty()
    }

    override fun readNbt(nbt: NbtCompound) {
        jarId = nbt.getLong("jarId")
        lastTick = nbt.getLong("lastTick")
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putLong("jarId", jarId)
        nbt.putLong("lastTick", lastTick)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return NbtCompound().also(::writeNbt)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.of(this)
    }
}
