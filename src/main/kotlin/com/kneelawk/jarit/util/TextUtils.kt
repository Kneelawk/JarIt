package com.kneelawk.jarit.util

import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

fun coordsText(pos: BlockPos): Text {
    return Texts.bracketed(
        Text.translatable("chat.coordinates", pos.x.toString(), pos.y.toString(), pos.z.toString())
    ).styled { style: Style ->
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
