package net.refractored.bloodmoonreloaded

import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import revxrsal.commands.bukkit.BukkitCommandHandler

class BloodmoonPlugin : LibreforgePlugin() {
    lateinit var handler: BukkitCommandHandler

    override fun loadConfigCategories(): List<ConfigCategory> =
        listOf(
            World,
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
//        handler.registerBrigadier()
//
//        Conditions.register(IsBoosterActive)
//
//        registerGenericHolderProvider {
//            RegisteredBoosters.getActiveBoosters().map { SimpleProvidedHolder(it) }
//        }
    }

    override fun handleReload() {
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
