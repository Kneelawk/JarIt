package com.kneelawk.jarit.item

import com.kneelawk.jarit.Constants.msg
import com.kneelawk.jarit.JarItConfig
import com.kneelawk.jarit.Log
import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.dimension.JarPlacement
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.*
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class JarCorkItem(settings: Settings) : Item(settings) {
    companion object {
        private fun isValid(world: World, pos: BlockPos, player: PlayerEntity?): Boolean {
            if (world.getBlockState(pos).block != Blocks.JAR_GLASS) {
                // we really only need to check this on one side
                if (!world.isClient) {
                    player?.sendMessage(msg("error.not_jar_glass"), false)
                }
                return false
            }

            // currently only supports using a cork on the top of a jar
            if (world.getBlockState(pos.offset(Direction.DOWN)).block == Blocks.JAR_GLASS || world.getBlockState(
                    pos.offset(Direction.UP)
                ).block == Blocks.JAR_GLASS
            ) {
                if (!world.isClient) player?.sendMessage(msg("error.not_ceiling"), false)
                return false
            }

            return true
        }

        private fun count(world: World, pos: BlockPos, dir: Direction): Int {
            val mut = pos.mutableCopy()
            var count = 0
            while (world.getBlockState(mut).block == Blocks.JAR_GLASS) {
                mut.move(dir)
                count++
            }
            return count
        }

        private fun checkDirection(
            world: World, starting: BlockPos, current: BlockPos, counted: Int, dir: Direction, player: PlayerEntity?
        ): Boolean {
            val new = count(world, current, dir)
            if (new < counted) {
                if (!world.isClient) {
                    player?.sendMessage(
                        msg(
                            "error.gap",
                            coordsText(current.offset(dir, new)),
                            coordsText(starting.offset(dir, new))
                        ), false
                    )
                }
                return false
            }
            if (new > counted) {
                if (!world.isClient) {
                    player?.sendMessage(
                        msg(
                            "error.gap",
                            coordsText(starting.offset(dir, counted)),
                            coordsText(current.offset(dir, counted))
                        ), false
                    )
                }
                return false
            }
            return true
        }

        private fun checkDown(
            world: World, starting: BlockPos, current: BlockPos, counted: Int, player: PlayerEntity?
        ): Boolean {
            val new = count(world, current, Direction.DOWN)
            if (new < counted) {
                if (!world.isClient) {
                    player?.sendMessage(
                        msg(
                            "error.gap",
                            coordsText(current.offset(Direction.DOWN, new)),
                            coordsText(starting.offset(Direction.DOWN, new))
                        ), false
                    )
                }
                return false
            }
            if (new > counted) {
                if (!world.isClient) {
                    player?.sendMessage(
                        msg(
                            "error.gap",
                            coordsText(starting.offset(Direction.DOWN, counted)),
                            coordsText(current.offset(Direction.DOWN, counted))
                        ), false
                    )
                }
                return false
            }
            return true
        }

        private fun coordsText(pos: BlockPos): Text {
            return Texts.bracketed(
                Text.translatable("chat.coordinates", pos.x.toString(), pos.y.toString(), pos.z.toString())
            )
                .styled { style: Style ->
                    style.withColor(Formatting.GREEN)
                        .withClickEvent(
                            ClickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                "/tp @s ${pos.x} ${pos.y} ${pos.z}"
                            )
                        )
                        .withHoverEvent(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip")
                            )
                        )
                }
        }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val pos = context.blockPos
        val player = context.player

        if (player == null && !JarItConfig.INSTANCE.nonPlayerUsable) {
            return ActionResult.FAIL
        }

        if (player?.canModifyBlocks() == false && !JarItConfig.INSTANCE.adventureModeUsable) {
            if (!world.isClient) {
                player.sendMessage(msg("error.adventure"), true)
            }
            return ActionResult.FAIL
        }

        if (!isValid(world, pos, player)) return ActionResult.FAIL

        val xpc = count(world, pos, Direction.EAST)
        val xnc = count(world, pos, Direction.WEST)
        val zpc = count(world, pos, Direction.SOUTH)
        val znc = count(world, pos, Direction.NORTH)

        // check ceiling
        val mut = BlockPos.Mutable()
        for (z in -(znc - 1) until zpc) {
            mut.set(pos, 0, 0, z)

            if (!checkDirection(world, pos, mut, xpc, Direction.EAST, player)) return ActionResult.FAIL

            if (!checkDirection(world, pos, mut, xnc, Direction.WEST, player)) return ActionResult.FAIL
        }

        val width = xpc + xnc - 1
        val depth = zpc + znc - 1
        val corner = pos.subtract(BlockPos(xnc - 1, 0, znc - 1))

        val ync = count(world, corner, Direction.DOWN)
        val height = ync

        if (!world.isClient) {
            Log.log.info("Jar: $width x $height x $depth")
        }

        // check x- side
        for (z in 0 until depth) {
            mut.set(corner, 0, 0, z)
            if (!checkDown(world, corner, mut, ync, player)) return ActionResult.FAIL
        }

        // check z- side
        for (x in 0 until width) {
            mut.set(corner, x, 0, 0)
            if (!checkDown(world, corner, mut, ync, player)) return ActionResult.FAIL
        }

        // check x+ side
        for (z in 0 until depth) {
            mut.set(corner, width - 1, 0, z)
            if (!checkDown(world, corner, mut, ync, player)) return ActionResult.FAIL
        }

        // check z+ side
        for (x in 0 until width) {
            mut.set(corner, x, 0, depth - 1)
            if (!checkDown(world, corner, mut, ync, player)) return ActionResult.FAIL
        }

        val start = pos.subtract(BlockPos(xnc - 1, height - 1, znc - 1))

        // check floor
        for (z in 0 until depth) {
            for (x in 0 until width) {
                mut.set(start, x, 0, z)
                if (world.getBlockState(mut).block != Blocks.JAR_GLASS) {
                    if (!world.isClient) {
                        player?.sendMessage(
                            msg(
                                "error.gap",
                                coordsText(mut),
                                coordsText(start)
                            ), false
                        )
                    }
                    return ActionResult.FAIL
                }
            }
        }

        if (width != depth || depth != height) {
            if (!world.isClient)
                player?.sendMessage(
                    msg("error.not_square", width, height, depth), false
                )
            return ActionResult.FAIL
        }

        val externalJarSize = width

        if (externalJarSize < 3) {
            if (!world.isClient)
                player?.sendMessage(msg("error.too_small", externalJarSize), false)
            return ActionResult.FAIL
        }

        if (externalJarSize > 64) {
            if (!world.isClient)
                player?.sendMessage(msg("error.too_large", externalJarSize), false)
            return ActionResult.FAIL
        }

        if (world.isClient || world !is ServerWorld) return ActionResult.SUCCESS

        JarPlacement.capture(world, start, externalJarSize)

        return ActionResult.CONSUME
    }
}
