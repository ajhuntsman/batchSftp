package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.KsftpLog
import com.ajhuntsman.ksftp.SftpConnectionParameters
import org.apache.commons.lang3.StringUtils

/**
 * Downloads one or more files.
 */
internal class DownloadTask(sftpConnectionParameters: SftpConnectionParameters, filePairs: List<FilePair>) : BaseTask(sftpConnectionParameters, filePairs) {

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

            KsftpLog.logInfo("Took " + KsftpLog.formatMillis(System.currentTimeMillis() - startTime) +
                    " to process " + filePairs.size + " file downloads")

            return true
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
            throw e
        }

    }
}