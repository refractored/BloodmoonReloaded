package net.refractored.bloodmoonreloaded

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * I use this to check if the plugin is purchased from polymart.
 * If it isn't, then it just kinda trys to motivate you to purchase.
 */
object Polymart {
    private const val LICENSE = "%%__LICENSE__%%"
    private const val RESOURCE_ID = "%%__RESOURCE__%%"

    fun checkPolymartStatus(): Boolean {
        val url = URI("https://api.polymart.org/v1/status").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        return try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                response.trim().equals("ok", ignoreCase = true)
            } else {
                if (BloodmoonPlugin.instance.configYml.getBool("disable-purchase-message")) return false
                BloodmoonPlugin.instance.logger.info("Polymart API is not reachable. (1)")
                false
            }
        } catch (e: SocketTimeoutException) {
            if (BloodmoonPlugin.instance.configYml.getBool("disable-purchase-message")) return false
            BloodmoonPlugin.instance.logger.info("Polymart API is not reachable. (1)")
            false
        } finally {
            connection.disconnect()
        }
    }

    fun verifyPurchase(): Boolean {
        val encodedLicense = URLEncoder.encode(LICENSE.replace(" ", "-"), StandardCharsets.UTF_8)
        val encodedResourceId = URLEncoder.encode(RESOURCE_ID, StandardCharsets.UTF_8)
        val url = URI("https://api.polymart.org/v1/verifyPurchase/?license=$encodedLicense&resource_id=$encodedResourceId").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        return try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.getJSONObject("response").getBoolean("success")
            } else {
                if (BloodmoonPlugin.instance.configYml.getBool("disable-purchase-message")) return false
                BloodmoonPlugin.instance.logger.info("Polymart API is not reachable. (2)")
                false
            }
        } catch (e: SocketTimeoutException) {
            if (BloodmoonPlugin.instance.configYml.getBool("disable-purchase-message")) return false
            BloodmoonPlugin.instance.logger.info("Polymart API is not reachable. (2)")
            false
        } finally {
            connection.disconnect()
        }
    }
}
