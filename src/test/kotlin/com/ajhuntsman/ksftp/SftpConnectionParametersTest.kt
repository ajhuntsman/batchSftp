package com.ajhuntsman.ksftp

import junit.framework.TestCase
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Unit tests for {@see ConnectionParameters}.
 */
class SftpConnectionParametersTest : TestCase() {

    @Test
    @Throws(Exception::class)
    fun testValidParameters() {
        SftpConnectionParametersBuilder.create().createConnectionParameters()
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
            SftpConnectionParametersBuilder.create().createConnectionParameters()
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
            SftpConnectionParametersBuilder.create().createConnectionParameters()
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
        SftpConnectionParametersBuilder.create().createConnectionParameters()
                .withHost("myserver.com")
                .withUsername("john.doe")
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testMissingUsername() {
        SftpConnectionParametersBuilder.create().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testMissingPassword() {
        SftpConnectionParametersBuilder.create().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withUsername("john.doe")
                .create()
    }

}