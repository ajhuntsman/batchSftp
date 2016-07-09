package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.KsftpLog
import com.ajhuntsman.ksftp.SftpConnectionParameters
import com.jcraft.jsch.SftpException
import org.apache.commons.lang3.StringUtils

/**
 * Checks for the existence of one or more remote files.
 */
internal class FilesExistTask(sftpConnectionParameters: SftpConnectionParameters, filePairs: List<FilePair>) : BaseTask(sftpConnectionParameters, filePairs) {

    override fun doWork(): Boolean {
        return checkFiles()
    }

    @Throws(Exception::class)
    private fun checkFiles(): Boolean {
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

                try {
                    sftpChannel?.ls(remotePath)
                } catch (e: SftpException) {
                    KsftpLog.logInfo("File doesn't exist: '$remotePath'")
                    return false
                }

            }

            KsftpLog.logInfo("Took " + KsftpLog.formatMillis(System.currentTimeMillis() - startTime) +
                    " to check " + filePairs.size + " files")

            return true
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
            throw e
        }

    }
}