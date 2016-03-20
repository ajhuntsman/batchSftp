package com.ajhuntsman.ksftp

/**
 * Encapsulates the parameters for an SFTP connection.
 */
data class ConnectionParameters(var host: String, val port: Int, val username: String?, val password: ByteArray?) {
}