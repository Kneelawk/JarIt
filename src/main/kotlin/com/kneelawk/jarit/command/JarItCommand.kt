package com.kneelawk.jarit.command

import com.kneelawk.jarit.Constants.msg
import com.kneelawk.jarit.Log
import com.kneelawk.jarit.block.Blocks
import com.kneelawk.jarit.blockentity.JarBlockEntity
import com.kneelawk.jarit.dimension.Dimensions
import com.kneelawk.jarit.dimension.JarDimensionInfo
import com.kneelawk.jarit.dimension.JarInfo
import com.kneelawk.jarit.dimension.JarPlacement
import com.kneelawk.jarit.item.JarBlockItem
import com.kneelawk.jarit.util.coordsText
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.TeleportTarget
import org.quiltmc.qkl.wrapper.minecraft.brigadier.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.argument.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.required
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.sendFeedback
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.server
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.world
import org.quiltmc.qkl.wrapper.minecraft.math.plus
import org.quiltmc.qsl.command.api.CommandRegistrationCallback
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions

object JarItCommand {
    fun init() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register("jar-it") {
                requires { it.hasPermissionLevel(2) }
                setupCreate()
                setupDestroy()
                setupEnter()
                setupGive()
                setupList()
            }
        }
    }

    private fun LiteralArgumentBuilder<ServerCommandSource>.setupCreate() {
        required(literal("create"), integer("size", 3, 64)) { _, size ->
            optional(long("id")) { id ->
                executeWithResult {
                    if (id == null) {
                        val jarDimInfo = JarDimensionInfo.get(server) ?: return@executeWithResult CommandResult.failure(
                            msg("error.no_jar_dim")
                        )
                        val fullJarSize = size().value()

                        val jarInfo = when (val res = JarPlacement.createNewJar(server, fullJarSize)) {
                            JarPlacement.JarCreateResult.JarAlreadyExists -> throw AssertionError() // shouldn't ever happen
                            JarPlacement.JarCreateResult.NoJarDimension -> return@executeWithResult CommandResult.failure(
                                msg("error.no_jar_dim")
                            )
                            is JarPlacement.JarCreateResult.Success -> res.info
                        }

                        giveJar(jarInfo.jarId)

                        sendFeedback(msg("create.success", jarText(server, jarInfo, jarDimInfo.maxJarSize)))

                        CommandResult.success()
                    } else {
                        val jarId = id().value()
                        val jarDimInfo = JarDimensionInfo.get(server) ?: return@executeWithResult CommandResult.failure(
                            msg("error.no_jar_dim")
                        )
                        val fullJarSize = size().value()

                        val (jarInfo, exists) = when (val res =
                            JarPlacement.createJarWithId(server, fullJarSize, jarId)) {
                            JarPlacement.JarCreateResult.JarAlreadyExists -> (JarInfo(jarId, fullJarSize - 2) to true)
                            JarPlacement.JarCreateResult.NoJarDimension -> return@executeWithResult CommandResult.failure(
                                msg("error.no_jar_dim")
                            )
                            is JarPlacement.JarCreateResult.Success -> (res.info to false)
                        }

                        giveJar(jarId)

                        if (exists) {
                            CommandResult.failure(
                                msg("error.jar_exists", jarId, jarText(server, jarInfo, jarDimInfo.maxJarSize))
                            )
                        } else {
                            sendFeedback(msg("create.success", jarText(server, jarInfo, jarDimInfo.maxJarSize)))
                            CommandResult.success()
                        }
                    }
                }
            }
        }
    }

    private fun LiteralArgumentBuilder<ServerCommandSource>.setupDestroy() {
        required(literal("destroy"), long("id")) { _, id ->
            suggests(JarIdSuggestionProvider)
            optional(literal("force")) { force ->
                executeWithResult {
                    val jarId = id().value()
                    val jarDim = JarPlacement.getJarDimension(server) ?: return@executeWithResult CommandResult.failure(
                        msg("error.no_jar_dim")
                    )
                    val jarDimInfo = JarDimensionInfo.get(jarDim)
                    val jarInfo = jarDimInfo.getJar(jarId) ?: return@executeWithResult CommandResult.failure(
                        msg("error.no_jar", jarId)
                    )

                    if (force == null) {
                        val pos = JarPlacement.findSafeDestination(jarDim, jarInfo, jarDimInfo.maxJarSize)
                            ?: (JarPlacement.getJarStart(jarId, jarDimInfo.maxJarSize) + BlockPos(1, 1, 1))

                        val jarText = jarText(jarInfo, pos)
                        val confirmText = Texts.bracketed(msg("destroy.confirm", jarInfo.jarId, jarInfo.size + 2))
                            .styled {
                                it.withColor(Formatting.RED)
                                    .withClickEvent(
                                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jar-it destroy $jarId force")
                                    )
                                    .withHoverEvent(
                                        HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT, Text.literal("/jar-it destroy $jarId force")
                                        )
                                    )
                            }

                        sendFeedback(msg("destroy.check", jarText, confirmText))
                    } else {
                        JarPlacement.destroyJar(server, jarDim, jarDimInfo, jarInfo)

                        sendFeedback(msg("destroy.success", jarInfo.jarId, jarInfo.size + 2))
                    }

                    CommandResult.success()
                }
            }
        }
    }

    private fun LiteralArgumentBuilder<ServerCommandSource>.setupList() {
        required(literal("list")) {
            execute {
                val jarDimInfo = JarDimensionInfo.get(server) ?: run {
                    Log.log.error("Error getting jar dimension")
                    return@execute
                }
                sendFeedback(
                    msg(
                        "list",
                        Texts.join(jarDimInfo.listJars(), Text.literal(", ")) {
                            jarText(
                                server, it, jarDimInfo.maxJarSize
                            )
                        })
                )
            }
        }
    }

    private fun LiteralArgumentBuilder<ServerCommandSource>.setupGive() {
        required(literal("give"), long("id")) { _, id ->
            suggests(JarIdSuggestionProvider)
            execute {
                giveJar(id().value())
            }
        }
    }

    private fun LiteralArgumentBuilder<ServerCommandSource>.setupEnter() {
        required(literal("enter")) {
            optional(player("target")) { target ->
                required(literal("id"), long("id")) { _, id ->
                    suggests(JarIdSuggestionProvider)
                    executeWithResult { enter(id().value(), target?.invoke(this)?.value() ?: source.player) }
                }
                required(literal("at"), blockPos("jar-pos")) { _, jarPos ->
                    executeWithResult {
                        val pos = jarPos().value()
                        (world.getBlockEntity(pos) as? JarBlockEntity)?.let { jar ->
                            enter(jar.jarId, target?.invoke(this)?.value() ?: source.player)
                        } ?: CommandResult.failure(msg("error.no_jar_at", coordsText(pos)))
                    }
                }
            }
        }
    }

    private fun CommandContext<ServerCommandSource>.enter(jarId: Long, target: ServerPlayerEntity): CommandResult {
        val jarDim = JarPlacement.getJarDimension(server) ?: return CommandResult.failure(msg("error.no_jar_dim"))
        val jarDimInfo = JarDimensionInfo.get(jarDim)
        val jarInfo = jarDimInfo.getJar(jarId) ?: return CommandResult.failure(msg("error.no_jar", jarId))
        val pos = JarPlacement.findSafeDestination(jarDim, jarInfo, jarDimInfo.maxJarSize)
            ?: (JarPlacement.getJarStart(jarId, jarDimInfo.maxJarSize) + BlockPos(1, 1, 1))

        QuiltDimensions.teleport<ServerPlayerEntity>(
            target, jarDim, TeleportTarget(Vec3d.ofBottomCenter(pos), target.velocity, target.yaw, target.pitch)
        )

        return CommandResult.success()
    }

    private fun jarText(server: MinecraftServer, info: JarInfo, maxJarSize: Int): Text {
        val pos = JarPlacement.findSafeDestination(server, info)
            ?: (JarPlacement.getJarStart(info.jarId, maxJarSize) + BlockPos(1, 1, 1))
        return jarText(info, pos)
    }

    private fun jarText(info: JarInfo, pos: BlockPos): Text {
        val dimId = Dimensions.JAR_DIMENSION_WORLD_KEY.value

        return Texts.bracketed(
            msg("jar", info.jarId, info.size + 2)
        ).styled { style: Style ->
            style.withColor(Formatting.GREEN)
                .withClickEvent(
                    ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/execute in $dimId run tp @s ${pos.x} ${pos.y} ${pos.z}"
                    )
                )
                .withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip")
                    )
                )
        }
    }

    private fun CommandContext<ServerCommandSource>.giveJar(jarId: Long) {
        // optional version of getPlayer()
        source.method_44023()?.let { player ->
            val stack = ItemStack(Blocks.JAR)
            JarBlockItem.setJarId(stack, jarId)
            giveStack(stack, player)
        }
    }

    private fun giveStack(stack: ItemStack, player: ServerPlayerEntity) {
        val bl = player.inventory.insertStack(stack)
        if (bl && stack.isEmpty) {
            stack.count = 1
            player.dropItem(stack, false)?.setDespawnImmediately()
            player.world
                .playSound(
                    null,
                    player.x,
                    player.y,
                    player.z,
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS,
                    0.2f,
                    ((player.random.nextFloat() - player.random.nextFloat()) * 0.7f + 1.0f) * 2.0f
                )
            player.currentScreenHandler.sendContentUpdates()
        } else {
            val itemEntity = player.dropItem(stack, false)
            if (itemEntity != null) {
                itemEntity.resetPickupDelay()
                itemEntity.owner = player.getUuid()
            }
        }
    }
}
