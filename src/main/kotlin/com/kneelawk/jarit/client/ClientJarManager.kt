package com.kneelawk.jarit.client

import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents

object ClientJarManager {


    fun init() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->

        }
    }
}
