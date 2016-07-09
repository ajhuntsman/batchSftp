package com.ajhuntsman.ksftp

import com.ajhuntsman.ksftp.exception.UploadTimeoutException
import com.ajhuntsman.ksftp.task.*
import java.util.concurrent.*

/**
 * The SFTP client.
 */
class SftpClient(val sftpConnectionParameters: SftpConnectionParameters) {

    @Throws(UploadTimeoutException::class, InterruptedException::class)
    fun upload(localFilePath: String, remoteFilePath: String, timeoutInSeconds: Int): Boolean {
        return uploadHelper(listOf(UploadTask(sftpConnectionParameters, listOf(FilePair(localFilePath, remoteFilePath)))), timeoutInSeconds)
    }

    @Throws(UploadTimeoutException::class, InterruptedException::class)
    fun upload(filePairs: List<FilePair>, timeoutInSeconds: Int): Boolean {
        if (filePairs.isEmpty()) {
            return true
        }

        return uploadHelper(listOf(UploadTask(sftpConnectionParameters, filePairs)), timeoutInSeconds)
    }

    @Throws(UploadTimeoutException::class, InterruptedException::class)
    fun upload(filePairs: List<FilePair>, batchSize: Int, timeoutInSeconds: Int): Boolean {
        if (filePairs.isEmpty()) {
            return true
        }

        // Partition the files into batches and create one task for each batch
        val tasks = mutableListOf<UploadTask>()
        filePairs.asSequence().batch(batchSize).forEach { group ->
            tasks.add(UploadTask(sftpConnectionParameters, group))
        }

        return uploadHelper(tasks, timeoutInSeconds)
    }

    @Throws(UploadTimeoutException::class, InterruptedException::class)
    private fun uploadHelper(uploadTasks: List<UploadTask>, timeoutInSeconds: Int): Boolean {
        if (uploadTasks.isEmpty()) {
            return true
        }

        val threadPool = Executors.newSingleThreadExecutor()
        val futures = mutableListOf<Future<Boolean>>()
        var success = true
        var nbrFiles = 0
        try {
            // Queue up tasks
            for (task in uploadTasks) {
                nbrFiles += task.filePairs.size
                futures.add(threadPool.submit(task))
            }

            // If the timeout was invalid, set a default of 1 hour per file
            var adjustedTimeoutInSeconds = timeoutInSeconds
            if (adjustedTimeoutInSeconds < 1) {
                adjustedTimeoutInSeconds = 60*60*nbrFiles
            }

            // Block & wait for tasks to finish
            threadPool.shutdown()
            val timedOut = !threadPool.awaitTermination(adjustedTimeoutInSeconds.toLong(), TimeUnit.SECONDS)
            if (timedOut) {
                val msg = "Upload of " + nbrFiles + " files timed out after " + adjustedTimeoutInSeconds + " seconds!"
                KsftpLog.logError(msg)
                throw UploadTimeoutException(msg)
            }

            // Return false if any task failed
            for (future in futures) {
                try {
                    if (!future.get()) success = false
                } catch (e: InterruptedException) {
                    KsftpLog.logError("Interrupted while getting task future!")
                    success = false
                } catch (e: CancellationException) {
                    KsftpLog.logError("Task future was canceled; likely because of a timeout")
                    success = false
                } catch (e: ExecutionException) {
                    KsftpLog.logError("Execution exception while getting task future -> " + e)
                    success = false
                }
            }
        } catch (e: InterruptedException) {
            KsftpLog.logError("Interrupted while waiting for tasks to finish!")
            throw e
        } finally {
            threadPool.shutdownNow()
        }
        return success
    }

    @Throws(Exception::class)
    fun download(localFilePath: String, remoteFilePath: String): Boolean {
        return DownloadTask(sftpConnectionParameters, listOf(FilePair(localFilePath, remoteFilePath)))
                .call()!!
    }

    @Throws(Exception::class)
    fun download(filePairs: List<FilePair>): Boolean {
        return DownloadTask(sftpConnectionParameters, filePairs)
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
        return FilesExistTask(sftpConnectionParameters, filePairs)
                .call()!!
    }

    @Throws(Exception::class)
    fun rename(filePair: FilePair): Boolean {
        return rename(listOf(filePair))
    }

    @Throws(Exception::class)
    fun rename(filePairs: List<FilePair>): Boolean {
        return RenameTask(sftpConnectionParameters, filePairs)
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
        return DeleteTask(sftpConnectionParameters, filePairs)
                .call()!!
    }

    companion object Factory {
        fun create(sftpConnectionParameters: SftpConnectionParameters): SftpClient = SftpClient(sftpConnectionParameters)
    }

    private fun <T> Sequence<T>.batch(n: Int): Sequence<List<T>> {
        return BatchingSequence(this, n)
    }

    private class BatchingSequence<T>(val source: Sequence<T>, val batchSize: Int) : Sequence<List<T>> {
        override fun iterator(): Iterator<List<T>> = object : AbstractIterator<List<T>>() {
            val iterate = if (batchSize > 0) source.iterator() else emptyList<T>().iterator()
            override fun computeNext() {
                if (iterate.hasNext()) setNext(iterate.asSequence().take(batchSize).toList())
                else done()
            }
        }
    }
}
