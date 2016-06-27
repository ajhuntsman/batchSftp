package com.ajhuntsman.ksftp.task

import com.ajhuntsman.ksftp.ConnectionParameters
import com.ajhuntsman.ksftp.FilePair
import com.ajhuntsman.ksftp.KsftpLog
import com.jcraft.jsch.SftpException
import org.apache.commons.lang3.StringUtils
import java.io.File

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

            var remotePath: String
            for (filePair in filePairs) {
                remotePath = filePair.sourceFilePath

                try {
                    deleteFile(remotePath)
                } catch (e: SftpException) {
                    KsftpLog.logError("Couldn't delete file: '$remotePath': " + e.message)
                    throw e
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

    @Throws(SftpException::class)
    private fun deleteFile(remotePath: String) {
        // If the file doesn't exist, move on
        if (StringUtils.isEmpty(remotePath) || StringUtils.equals(".", remotePath) || StringUtils.equals("..", remotePath)) {
            //KsftpLog.logDebug("Skipping invalid file: '$remotePath'")
            return
        }

        try {
            sftpChannel?.ls(remotePath)
        } catch (e: SftpException) {
            KsftpLog.logInfo("File doesn't exist: '$remotePath'")
            return
        }

        if (isDirectory(remotePath)) {
            // Change to the directory
            sftpChannel?.cd(remotePath)

            // Recursively delete all files in this directory
            val lsEntries = getLsEntriesForCurrentDirectory(sftpChannel)
            //KsftpLog.logDebug("Found '${lsEntries.size}' files in: '$remotePath'")
            for (entry in lsEntries) {
                deleteFile(entry.filename)
            }

            // NOTE: We can't delete a directory we are in, so we must change to the parent directory
            val fullDirectoryPath = sftpChannel?.pwd()
            if (sftpChannel?.pwd()?.endsWith(remotePath) ?: false) {
                val parentDirectory = fullDirectoryPath?.substring(0, fullDirectoryPath.lastIndexOf(File.separatorChar))
                //KsftpLog.logDebug("Changing to directory: '$parentDirectory'...")
                sftpChannel?.cd(parentDirectory);
            }

            //KsftpLog.logDebug("Attempting to delete directory: '$fullDirectoryPath'...")
            sftpChannel?.rmdir(fullDirectoryPath);
            //runExeCommand("rm -rf " + fullDirectoryPath)
        } else {
            //KsftpLog.logDebug("Attempting to delete file: '$remotePath'...")
            sftpChannel?.rm(remotePath);
        }
    }

    @Throws(SftpException::class)
    private fun isDirectory(remotePath: String): Boolean {
        return sftpChannel?.stat(remotePath)?.isDir ?: false
    }
}