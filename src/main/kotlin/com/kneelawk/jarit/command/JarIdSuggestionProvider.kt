package com.kneelawk.jarit.command

import com.kneelawk.jarit.Constants.msg
import com.kneelawk.jarit.dimension.JarDimensionInfo
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.server
import java.util.concurrent.CompletableFuture

object JarIdSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val jarDimInfo = JarDimensionInfo.get(context.server) ?: return CompletableFuture.failedFuture(
            IllegalStateException("Missing jar-it:jar-dimension")
        )

        jarDimInfo.listJars().forEach { builder.suggest(it.jarId.toString(), msg("jar", it.jarId, it.size)) }

        return builder.buildFuture()
    }
}
