package net.refractored.bloodmoonreloaded

import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import net.refractored.bloodmoonreloaded.libreforge.IsBloodmoonActive
import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry
import revxrsal.commands.bukkit.BukkitCommandHandler

class BloodmoonPlugin : LibreforgePlugin() {
    lateinit var handler: BukkitCommandHandler

    override fun loadConfigCategories(): List<ConfigCategory> =
        listOf(
            BloodmoonRegistry,
        )

    override fun handleEnable() {
        instance = this

        handler = BukkitCommandHandler.create(this)

//        handler.setExceptionHandler(CommandErrorHandler())
//
//        val boosterResolver = BoosterResolver()
//
//        handler.autoCompleter.registerParameterSuggestions(Booster::class.java, boosterResolver)
//
//        handler.registerValueResolver(Booster::class.java, boosterResolver)
//
        handler.registerBrigadier()

        Conditions.register(IsBloodmoonActive)

        registerGenericHolderProvider {
            BloodmoonRegistry.getActiveWorlds().map { SimpleProvidedHolder(it) }
        }
    }

    override fun handleReload() {
        // idk whawt to do lol
    }

    override fun handleDisable() {
        if (this::handler.isInitialized) {
            handler.unregisterAllCommands()
        }
    }

    companion object {
        lateinit var instance: BloodmoonPlugin
            private set
    }
}
