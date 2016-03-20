package com.ajhuntsman.batchsftp

import junit.framework.TestCase
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Unit tests for {@see ConnectionParameters}.
 */
class ConnectionParametersTest : TestCase() {

    @Test
    @Throws(Exception::class)
    fun testValidParameters() {
        ConnectionParametersBuilder.create().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withUsername("john.doe")
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyHost() {
        assertFailsWith(IllegalArgumentException::class) {
            ConnectionParametersBuilder.create().createConnectionParameters()
                    .withPort(2222)
                    .withUsername("john.doe")
                    .withPassword("1234".toByteArray())
                    .create()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testInvalidHost() {
        assertFailsWith(IllegalArgumentException::class) {
            ConnectionParametersBuilder.create().createConnectionParameters()
                    .withHost("")
                    .withPort(2222)
                    .withUsername("john.doe")
                    .withPassword("1234".toByteArray())
                    .create()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMissingPort() {
        ConnectionParametersBuilder.create().createConnectionParameters()
                .withHost("myserver.com")
                .withUsername("john.doe")
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testMissingUsername() {
        ConnectionParametersBuilder.create().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testMissingPassword() {
        ConnectionParametersBuilder.create().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withUsername("john.doe")
                .create()
    }

}