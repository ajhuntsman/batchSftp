package com.ajhuntsman.batchsftp.task

import com.ajhuntsman.batchsftp.ConnectionParameters
import com.ajhuntsman.batchsftp.FilePair
import com.ajhuntsman.batchsftp.SftpLog
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.util.concurrent.Callable

/**
 * The base class for all tasks.
 */
internal abstract class BaseTask(val connectionParameters: ConnectionParameters, var filePairs: List<FilePair>) : Callable<Boolean>, Comparable<BaseTask> {

    private var session: Session? = null
    protected var sftpChannel: ChannelSftp? = null

    @Throws(Exception::class)
    override fun call(): Boolean? {
        try {
            // Setup the connection
            setupSftpConnection()

            // Have subclasses do their work
            return doWork()
        } catch (e: Exception) {
            SftpLog.logSevere(e.message)
            return false
        } finally {
            tearDownSftpConnection()
        }
    }

    override fun compareTo(other: BaseTask): Int {
        if (this === other) return 0
        return Integer.valueOf(filePairs.size)!!.compareTo(other.filePairs.size)
    }

    /**
     * Sets up the [ChannelSftp].
     */
    @Throws(Exception::class)
    private fun setupSftpConnection() {
        val jsch = JSch()
        try {
            // Create a session
            session = jsch.getSession(
                    connectionParameters.username,
                    connectionParameters.host,
                    connectionParameters.port)
            session!!.setConfig("StrictHostKeyChecking", "no")

            if (connectionParameters.password != null) {
                session!!.setPassword(connectionParameters.password)
            }

            SftpLog.logFine("Attempting to create SFTP session...")
            session!!.connect()
            SftpLog.logFine("SFTP session established")

            // Open an SFTP connection
            sftpChannel = session!!.openChannel("sftp") as ChannelSftp
            SftpLog.logFine("Attempting to create SFTP channel...")
            sftpChannel!!.connect()
            SftpLog.logFine("SFTP channel established")
        } catch (e: Exception) {
            SftpLog.logSevere(e.message)
            throw e
        }

    }

    /**
     * Tears down the [ChannelSftp].
     */
    private fun tearDownSftpConnection() {
        if (sftpChannel != null) {
            sftpChannel!!.exit()
        }
        if (session != null) {
            session!!.disconnect()
        }
    }

    /**
     * Factory method to be implemented by subclasses.
     */
    @Throws(Exception::class)
    protected abstract fun doWork(): Boolean
}