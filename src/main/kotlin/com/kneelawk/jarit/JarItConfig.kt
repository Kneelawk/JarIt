package com.kneelawk.jarit

import org.quiltmc.config.api.Config
import org.quiltmc.config.api.ConfigEnvironment
import org.quiltmc.config.api.WrappedConfig
import org.quiltmc.config.api.annotations.Comment
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.impl.config.Json5Serializer

class JarItConfig : WrappedConfig() {
    companion object {
        private val ENV = ConfigEnvironment(QuiltLoader.getConfigDir(), Json5Serializer.INSTANCE)

        val INSTANCE: JarItConfig = Config.create(ENV, Constants.MOD_ID, "config", JarItConfig::class.java)

        fun ensureInit() {
            if (INSTANCE.adventureModeUsable) {
                Log.log.info("Jar It! Jars can be used in adventure mode!")
            }
        }
    }

    @Comment("Whether adventure-mode players should be able to use the cork or opener items.")
    val adventureModeUsable: Boolean = false

    @Comment("Whether non-players should be able to use the cork or opener items.")
    val nonPlayerUsable: Boolean = true
}
