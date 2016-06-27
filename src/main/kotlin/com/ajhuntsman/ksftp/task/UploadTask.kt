package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.ConnectionParameters
import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.KsftpLog
import com.jcraft.jsch.SftpException
import org.apache.commons.lang3.StringUtils
import java.io.File

/**
 * Uploads one or more files.
 */
internal class UploadTask(connectionParameters: ConnectionParameters, filePairs: List<FilePair>) : BaseTask(connectionParameters, filePairs) {

    override fun doWork(): Boolean {
        return uploadFiles()
    }

    @Throws(Exception::class)
    private fun uploadFiles(): Boolean {
        if (filePairs.isEmpty()) {
            return true
        }

        try {
            val startTime = System.currentTimeMillis()

            // Hold onto the present working directory
            val pwd = sftpChannel?.pwd()

            // Upload every file
            var localFilePath: String
            var remoteFilePath: String
            for (filePair in filePairs) {
                localFilePath = filePair.sourceFilePath
                remoteFilePath = filePair.destinationFilePath

                if (StringUtils.isEmpty(localFilePath)) {
                    continue
                }

                val localFile = File(localFilePath)
                if (!localFile.isFile) {
                    KsftpLog.logError("Missing local file '$localFilePath'")
                    continue
                }

                // Lazily create the directory structure
                val remoteDirectoryPath = remoteFilePath.substring(0, remoteFilePath.lastIndexOf(File.separator))
                try {
                    sftpChannel?.cd(remoteDirectoryPath)
                } catch (e: SftpException) {
                    sftpChannel?.mkdir(remoteDirectoryPath)
                    KsftpLog.logInfo("Created remote directory '$remoteDirectoryPath'")
                } finally {
                    // Get back to our starting directory
                    sftpChannel?.cd(pwd)
                }

                // Upload the file
                sftpChannel?.put(localFilePath, remoteFilePath)
                KsftpLog.logInfo("Uploaded '$localFilePath' to '$remoteFilePath'")
            }

            //try {Thread.sleep(1000*10);} catch (e: Exception) {/* no-op */}

            KsftpLog.logInfo("Took " + KsftpLog.formatMillis(System.currentTimeMillis() - startTime) +
                    " to process " + filePairs.size + " file uploads")

            return true
        } catch (e: Exception) {
            KsftpLog.logError(e.message)
            throw e
        }

    }
}