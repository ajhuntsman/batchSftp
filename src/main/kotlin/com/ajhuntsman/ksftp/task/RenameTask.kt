package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.ConnectionParameters
import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.SftpLog
import com.jcraft.jsch.SftpException
import org.apache.commons.lang3.StringUtils
import java.io.File

/**
 * Renames one or more remote files.
 */
internal class RenameTask(connectionParameters: ConnectionParameters, filePairs: List<FilePair>) : BaseTask(connectionParameters, filePairs) {

    override fun doWork(): Boolean {
        return renameFiles()
    }

    @Throws(Exception::class)
    private fun renameFiles(): Boolean {
        if (filePairs.isEmpty()) {
            return true
        }

        var success: Boolean = true
        try {
            val startTime = System.currentTimeMillis()

            // Hold onto the present working directory
            val pwd = sftpChannel?.pwd()

            // Rename every file
            var oldRemotePath: String
            var newRemotePath: String
            for (filePair in filePairs) {
                oldRemotePath = filePair.sourceFilePath
                newRemotePath = filePair.destinationFilePath

                if (StringUtils.isEmpty(oldRemotePath)) {
                    continue
                }

                // Lazily create the directory structure
                val remoteDirectoryPath = newRemotePath.substring(0, newRemotePath.lastIndexOf(File.separator))
                try {
                    sftpChannel?.cd(remoteDirectoryPath)
                } catch (e: SftpException) {
                    sftpChannel?.mkdir(remoteDirectoryPath)
                    SftpLog.logInfo("Created remote directory '$remoteDirectoryPath'")
                } finally {
                    // Get back to our starting directory
                    sftpChannel?.cd(pwd)
                }

                try {
                    sftpChannel?.rename(oldRemotePath, newRemotePath)
                    SftpLog.logInfo("Renamed '$oldRemotePath' to '$newRemotePath'")
                } catch (e: SftpException) {
                    SftpLog.logSevere("Could not rename '" + oldRemotePath + "' to '" + newRemotePath + "' -> " + e.message)
                    success = false
                }

            }

            SftpLog.logInfo("Took " + SftpLog.formatMillis(System.currentTimeMillis() - startTime) +
                    " to process " + filePairs.size + " file renames")

            return success
        } catch (e: Exception) {
            SftpLog.logSevere(e.message)
            throw e
        }

    }
}