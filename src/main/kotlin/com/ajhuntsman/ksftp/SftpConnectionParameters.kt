package com.ajhuntsman.ksftp

/**
 * Encapsulates the parameters for an SFTP connection.
 */
data class SftpConnectionParameters(var host: String, val port: Int, val username: String?, val password: ByteArray?) {

}