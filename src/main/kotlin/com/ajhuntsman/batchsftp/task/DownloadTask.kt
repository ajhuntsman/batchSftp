package com.ajhuntsman.batchsftp.task

import com.ajhuntsman.batchsftp.ConnectionParameters
import com.ajhuntsman.batchsftp.FilePair
import com.ajhuntsman.batchsftp.SftpLog
import org.apache.commons.lang3.StringUtils

/**
 * Downloads one or more files.
 */
internal class DownloadTask(connectionParameters: ConnectionParameters, filePairs: List<FilePair>) : BaseTask(connectionParameters, filePairs) {

    override fun doWork(): Boolean {
        return downloadFiles()
    }

    @Throws(Exception::class)
    private fun downloadFiles(): Boolean {
        if (filePairs.isEmpty()) {
            return true
        }

        val startTime = System.currentTimeMillis()
        try {
            // Download every file
            var localFilePath: String
            var remoteFilePath: String
            for (filePair in filePairs) {
                localFilePath = filePair.sourceFilePath
                remoteFilePath = filePair.destinationFilePath

                if (StringUtils.isEmpty(localFilePath)) {
                    continue
                }

                // Download the file
                sftpChannel?.get(remoteFilePath, localFilePath)
            }

            SftpLog.logInfo("Took " + SftpLog.formatMillis(System.currentTimeMillis() - startTime) +
                    " to process " + filePairs.size + " file downloads")

            return true
        } catch (e: Exception) {
            SftpLog.logSevere(e.message)
            throw e
        }

    }
}