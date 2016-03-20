package com.ajhuntsman.batchsftp

import org.apache.commons.lang3.StringUtils
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

/**
 * The logger, which can be enabled or disabled via [Client].
 */
object SftpLog {

    val TAG = "com.ajhuntsman.batchsftp"

    private var enabled = true

    /**
     * Enables or disables logging; logging is enabled by default.
     *
     * @param allowLogging enables/disables logging
     */
    fun enableDisableLogging(allowLogging: Boolean) {
        enabled = allowLogging
    }

    /**
     * Returns a formatted string containing the minutes and seconds for the specified amount of
     * milliseconds.
     *
     * @param millis the number of milliseconds to format
     */
    fun formatMillis(millis: Long): String {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
    }

    /**
     * Parses the stack trace of the current thread into a meaningful string.
     */
    private fun parseStackTrace(): String {
        try {
            // Find the first stack element just before this class, for our classes
            val stackElements = Thread.currentThread().stackTrace
            for (stackElement in stackElements) {
                val fullClassName = stackElement.className
                if (fullClassName.startsWith(TAG) && !StringUtils.equals(SftpLog::class.java.name, fullClassName)) {
                    //val className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
                    val methodName = stackElement.methodName
                    val lineNumber = stackElement.lineNumber
                    return "$fullClassName.$methodName: $lineNumber: "
                }
            }
        } catch (exc: Exception) {
            // OK to ignore
        }

        return ""
    }

    /**
     * Logs the specified message at the [Level.FINE] level.
     *
     * @param message the message to log
     */
    fun logFine(message: String?) {
        log(Level.FINE, message)
    }

    /**
     * Logs the specified message at the [Level.INFO] level.
     *
     * @param message the message to log
     */
    fun logInfo(message: String?) {
        log(Level.INFO, message)
    }

    /**
     * Logs the specified message at the [Level.SEVERE] level.
     *
     * @param message the message to log
     */
    fun logSevere(message: String?) {
        log(Level.SEVERE, message)
    }

    private fun log(level: Level, msg: String?) {
        if (!enabled || StringUtils.isEmpty(msg)) {
            return
        }

        // Append the message to the current stack trace
        var formattedMessage = parseStackTrace() + msg

        Logger.getLogger(TAG).log(level, formattedMessage)
    }

}