package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.ConnectionParameters
import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.KsftpLog
import com.jcraft.jsch.SftpException
import org.apache.commons.lang3.StringUtils

/**
 * Deletes one more remote files.
 */
internal class DeleteTask(connectionParameters: ConnectionParameters, filePairs: List<FilePair>) : BaseTask(connectionParameters, filePairs) {

    override fun doWork(): Boolean {
        return deleteFiles()
    }

    @Throws(Exception::class)
    private fun deleteFiles(): Boolean {
        if (filePairs.isEmpty()) {
            return true
        }

        try {
            val startTime = System.currentTimeMillis()

            // Check every file
            var remotePath: String
            for (filePair in filePairs) {
                remotePath = filePair.sourceFilePath
                if (StringUtils.isEmpty(remotePath)) {
                    continue
                }

                // If the file doesn't exist, move on
                try {
                    sftpChannel?.ls(remotePath)
                } catch (e: SftpException) {
                    KsftpLog.logInfo("File doesn't exist: '$remotePath'")
                    continue
                }

                try {
                    sftpChannel?.rm(remotePath)
                } catch (e: SftpException) {
                    KsftpLog.logInfo("Couldn't delete file: '$remotePath'")
                    return false
                }

            }

            KsftpLog.logInfo("Took " + KsftpLog.formatMillis(System.currentTimeMillis() - startTime) +
                    " to delete " + filePairs.size + " files")

            return true
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
            throw e
        }

    }
}