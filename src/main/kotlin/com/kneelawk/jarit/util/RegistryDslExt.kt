package com.kneelawk.jarit.util

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.quiltmc.qkl.wrapper.minecraft.registry.RegistryDsl
import org.quiltmc.qkl.wrapper.minecraft.registry.RegistryScope

class M2RegistryAction<P, A>(
    val modid: String?, private val primary: Registry<P>, private val added: Registry<A>,
    private val defaultConvert: (P) -> A
) {
    private val actions = mutableListOf<M2RegisterAction<P, A>>()

    @RegistryDsl
    infix fun P.withId(id: String): M2RegisterAction<P, A> {
        val identifier = if (':' in id || modid == null) {
            Identifier(id)
        } else {
            Identifier(modid, id)
        }

        val action = M2RegisterAction(identifier, this, primary, added, defaultConvert)
        actions.add(action)
        return action
    }

    internal fun finish() {
        actions.forEach(M2RegisterAction<P, A>::apply)
    }
}

class M2RegisterAction<P, A>(
    private val id: Identifier, private val entry: P, private val primary: Registry<P>, private val added: Registry<A>,
    private val defaultConvert: (P) -> A
) {
    private var convert: ((P) -> A)? = null

    @RegistryDsl
    infix fun withAdded(newConvert: (P) -> A) {
        convert = newConvert
    }

    internal fun apply() {
        val entryConverter = convert ?: defaultConvert

        Registry.register(primary, id, entry)
        Registry.register(added, id, entryConverter(entry))
    }
}

@RegistryDsl
fun <P, A> RegistryScope.multi(
    primary: Registry<P>, added: Registry<A>, defaultConvert: (P) -> A, action: M2RegistryAction<P, A>.() -> Unit
) {
    M2RegistryAction(modid, primary, added, defaultConvert).apply(action).finish()
}
