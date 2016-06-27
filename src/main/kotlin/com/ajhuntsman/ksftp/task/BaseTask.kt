package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.ConnectionParameters
import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.KsftpLog
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.BufferedInputStream
import java.util.concurrent.Callable

/**
 * The base class for all tasks.
 */
internal abstract class BaseTask(val connectionParameters: ConnectionParameters, var filePairs: List<FilePair>) : Callable<Boolean>, Comparable<BaseTask> {

    private var session: Session? = null
    protected var sftpChannel: ChannelSftp? = null
    protected var execChannel: ChannelExec? = null

    @Throws(Exception::class)
    override fun call(): Boolean? {
        try {
            // Setup the connection
            setupSftpConnection()

            // Have subclasses do their work
            return doWork()
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
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

            KsftpLog.logDebug("Attempting to create SFTP session...")
            session!!.connect()
            KsftpLog.logDebug("SFTP session established")

            // Open an SFTP connection
            sftpChannel = session!!.openChannel("sftp") as ChannelSftp
            KsftpLog.logDebug("Attempting to create SFTP channel...")
            sftpChannel!!.connect()
            KsftpLog.logDebug("SFTP channel established")
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
            throw e
        }
    }

    /**
     * Tears down the [ChannelSftp].
     */
    private fun tearDownSftpConnection() {
        sftpChannel?.exit()
        session?.disconnect()
    }

    /**
     * Factory method to be implemented by subclasses.
     */
    @Throws(Exception::class)
    protected abstract fun doWork(): Boolean

    /**
     * Runs the specified command.
     *
     * @param command the command to execute
     */
    protected fun runExeCommand(command: String) {
        var inputStream: BufferedInputStream? = null
        try {
            execChannel = session!!.openChannel("exec") as ChannelExec
            if (execChannel == null) {
                throw RuntimeException("Could not create a ChannelExec")
            }

            execChannel!!.setCommand(command)
            execChannel!!.setErrStream(System.err)
            inputStream = execChannel?.inputStream?.buffered(1024) ?: throw RuntimeException("Could not create an input stream")
            KsftpLog.logDebug("Exec channel prepared with command: '$command'")

            execChannel!!.connect();
            while (inputStream.available() > 0) {
                if (inputStream.read() < 0) {
                    break;
                }
            }

            if (execChannel!!.isClosed) {
                KsftpLog.logDebug("Exit status: '${execChannel!!.exitStatus}'")
            }
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
            throw e
        } finally {
            inputStream?.close()
            execChannel!!.disconnect()
        }
    }

    /**
     * Returns a new mutable list of all file entries in the current directory of the specified [ChannelSftp].
     *
     * @param theSftpChannel the SFTP channel
     */
    fun getLsEntriesForCurrentDirectory(theSftpChannel: ChannelSftp?): MutableList<ChannelSftp.LsEntry> {
        // Create a typed collection of entries in the current directory
        //val test: Vector<*>? = theSftpChannel?.ls(theSftpChannel.pwd())
        val lsEntries = mutableListOf<ChannelSftp.LsEntry>()
        theSftpChannel?.ls(theSftpChannel.pwd())?.forEach { lsEntry ->
            if (lsEntry is ChannelSftp.LsEntry) {
                lsEntries.add(lsEntry)
            }
        }
        return lsEntries;
    }
}