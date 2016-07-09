package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.KsftpLog
import com.ajhuntsman.ksftp.SftpConnectionParameters
import com.jcraft.jsch.SftpException
import org.apache.commons.lang3.StringUtils
import java.io.File

/**
 * Renames one or more remote files.
 */
internal class RenameTask(sftpConnectionParameters: SftpConnectionParameters, filePairs: List<FilePair>) : BaseTask(sftpConnectionParameters, filePairs) {

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
                    KsftpLog.logInfo("Created remote directory '$remoteDirectoryPath'")
                } finally {
                    // Get back to our starting directory
                    sftpChannel?.cd(pwd)
                }

                try {
                    sftpChannel?.rename(oldRemotePath, newRemotePath)
                    KsftpLog.logInfo("Renamed '$oldRemotePath' to '$newRemotePath'")
                } catch (e: SftpException) {
                    KsftpLog.logError("Could not rename '" + oldRemotePath + "' to '" + newRemotePath + "' -> " + e.message)
                    success = false
                }

            }

            KsftpLog.logInfo("Took " + KsftpLog.formatMillis(System.currentTimeMillis() - startTime) +
                    " to process " + filePairs.size + " file renames")

            return success
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
            throw e
        }

    }
}