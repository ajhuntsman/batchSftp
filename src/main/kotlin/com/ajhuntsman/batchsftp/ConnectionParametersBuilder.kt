package com.ajhuntsman.batchsftp

import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Builds a new [ConnectionParameters].
 */
class ConnectionParametersBuilder {

    private var host: String? = null
    private var port: Int = 22
    private var username: String? = null
    private var password: ByteArray? = null

    /**
     * Creates a new builder.
     */
    fun createConnectionParameters(): ConnectionParametersBuilder {
        return ConnectionParametersBuilder()
    }

    /**
     * Gets the [ConnectionParameters] created by this builder.
     */
    @Throws(IllegalArgumentException::class)
    fun create(): ConnectionParameters {
        if (StringUtils.isEmpty(host)) {
            throw IllegalArgumentException("host cannot be blank")
        }

        return ConnectionParameters(host!!, port, username, password)
    }

    /**
     * Gets the value for the specified environment variable, or `null`.
     *
     * @param environmentVariableName the environment variable whose value should be returned
     */
    private fun getEnvironmentVariable(environmentVariableName: String): String? {
        return if (StringUtils.isNotEmpty(environmentVariableName)) System.getenv()[environmentVariableName] else null
    }

    /**
     * Sets the host to the specified value.
     *
     * @param host the value to use
     */
    fun withHost(host: String): ConnectionParametersBuilder {
        this.host = host
        return this
    }

    /**
     * Sets the host using the specified environment variable.
     *
     * @param environmentVariableName the name of the environment variable whose value should be used as the host
     */
    fun withHostFromEnvironmentVariable(environmentVariableName: String): ConnectionParametersBuilder {
        this.host = getEnvironmentVariable(environmentVariableName)
        return this
    }

    /**
     * Sets the port to the specified value.
     *
     * @param port the value to use
     */
    fun withPort(port: Int): ConnectionParametersBuilder {
        this.port = port
        return this
    }

    /**
     * Sets the port using the specified environment variable.

     * @param environmentVariableName the name of the environment variable whose value should be used as the port
     */
    fun withPortFromEnvironmentVariable(environmentVariableName: String): ConnectionParametersBuilder {
        val environmentVariableValue = getEnvironmentVariable(environmentVariableName)
        if (StringUtils.isNotEmpty(environmentVariableValue)) {
            this.port = Integer.valueOf(environmentVariableValue)
        }
        return this
    }

    /**
     * Sets the username to the specified value.

     * @param username the value to use
     */
    fun withUsername(username: String): ConnectionParametersBuilder {
        this.username = username
        return this
    }

    /**
     * Sets the username using the specified environment variable.

     * @param environmentVariableName the name of the environment variable whose value should be used as the username
     */
    fun withUsernameFromEnvironmentVariable(environmentVariableName: String): ConnectionParametersBuilder {
        this.username = getEnvironmentVariable(environmentVariableName)
        return this
    }

    /**
     * Sets the password to the specified value.

     * @param password the value to use
     */
    fun withPassword(password: ByteArray): ConnectionParametersBuilder {
        this.password = Arrays.copyOf(password, password.size)
        return this
    }

    /**
     * Sets the password using the specified environment variable.

     * @param environmentVariableName the name of the environment variable whose value should be used as the password
     */
    fun withPasswordFromEnvironmentVariable(environmentVariableName: String): ConnectionParametersBuilder {
        val environmentVariableValue = getEnvironmentVariable(environmentVariableName)
        if (StringUtils.isNotEmpty(environmentVariableValue)) {
            this.password = environmentVariableValue?.toByteArray()
        }
        return this
    }

    companion object Factory {
        fun create(): ConnectionParametersBuilder = ConnectionParametersBuilder()
    }
}