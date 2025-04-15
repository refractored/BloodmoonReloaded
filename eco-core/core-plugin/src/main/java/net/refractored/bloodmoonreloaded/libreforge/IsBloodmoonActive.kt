package net.refractored.bloodmoonreloaded.libreforge

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld

object IsBloodmoonActive : Condition<NoCompileData>("is_bloodmoon_active") {
    override val arguments =
        arguments {
            require("world", "You must specify the world!")
        }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean = BloodmoonRegistry.getWorld(config.getString("world"))?.status == BloodmoonWorld.Status.ACTIVE
}
