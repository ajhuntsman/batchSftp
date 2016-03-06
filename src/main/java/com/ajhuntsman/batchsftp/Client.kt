package com.ajhuntsman.batchsftp

import com.ajhuntsman.batchsftp.task.*

/**
 * The SFTP client.
 */
class Client(val connectionParameters: ConnectionParameters) {

    @Throws(Exception::class)
    fun upload(localFilePath: String, remoteFilePath: String): Boolean {
        return UploadTask(connectionParameters, listOf(FilePair(localFilePath, remoteFilePath)))
                .call()!!
    }

    @Throws(Exception::class)
    fun upload(filePairs: List<FilePair>): Boolean {
        return UploadTask(connectionParameters, filePairs)
                .call()!!
    }

    @Throws(Exception::class)
    fun download(localFilePath: String, remoteFilePath: String): Boolean {
        return DownloadTask(connectionParameters, listOf(FilePair(localFilePath, remoteFilePath)))
                .call()!!
    }

    @Throws(Exception::class)
    fun download(filePairs: List<FilePair>): Boolean {
        return DownloadTask(connectionParameters, filePairs)
                .call()!!
    }

    @Throws(Exception::class)
    fun checkFile(remoteFilePath: String): Boolean {
        return checkFiles(listOf(remoteFilePath))
    }

    @Throws(Exception::class)
    fun checkFiles(remoteFilePaths: List<String>): Boolean {
        var filePairs: MutableList<FilePair> = mutableListOf()
        for (remoteFilePath in remoteFilePaths) {
            filePairs.add(FilePair(remoteFilePath, remoteFilePath))
        }
        return FilesExistTask(connectionParameters, filePairs)
                .call()!!
    }

    @Throws(Exception::class)
    fun rename(filePair: FilePair): Boolean {
        return rename(listOf(filePair))
    }

    @Throws(Exception::class)
    fun rename(filePairs: List<FilePair>): Boolean {
        return RenameTask(connectionParameters, filePairs)
                .call()!!
    }

    @Throws(Exception::class)
    fun delete(remoteFilePath: String): Boolean {
        return delete(listOf(remoteFilePath))
    }

    @Throws(Exception::class)
    fun delete(remoteFilePaths: List<String>): Boolean {
        var filePairs: MutableList<FilePair> = mutableListOf()
        for (remoteFilePath in remoteFilePaths) {
            filePairs.add(FilePair(remoteFilePath, remoteFilePath))
        }
        return DeleteTask(connectionParameters, filePairs)
                .call()!!
    }

    companion object Factory {
        fun create(connectionParameters: ConnectionParameters): Client = Client(connectionParameters)
    }
}