package net.refractored.bloodmoonreloaded.messages

import com.willfp.eco.core.config.base.LangYml
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object Messages {
    /**
     * Converts this component to a string using minimessage.
     */
    fun Component.toMinimessage(): String = MiniMessage.miniMessage().serialize(this)

    /**
     * Converts this string to a component using minimessage.
     */
    fun String.miniToComponent(): Component = MiniMessage.miniMessage().deserialize(this)

    fun Component.toPlaintext(): String = PlainTextComponentSerializer.plainText().serialize(this)

    /**
     * Returns a new string obtained by replacing all occurrences of the [oldValue] substring in this string
     * with the specified [newValue] component formatted as minimessage.
     *
     * If the object has a method to get the raw minimessage,
     * it's recommended to use that instead as this just converts it back to minimessage,
     */
    fun String.replace(
        oldValue: String,
        newValue: Component,
        ignoreCase: Boolean = false
    ): String = this.replace(oldValue, newValue.toMinimessage(), ignoreCase)

    /**
     * Gets the prefix from the lang.yml.
     */
    fun LangYml.miniPrefix(): String = this.getString("messages.prefix")

    /**
     * Gets a string from the lang.yml and adds a prefix.
     */
    fun LangYml.getStringPrefixed(key: String): String = this.miniPrefix() + this.getString(key)
}
