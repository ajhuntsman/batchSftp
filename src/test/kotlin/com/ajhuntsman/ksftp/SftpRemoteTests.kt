package com.ajhuntsman.ksftp

import junit.framework.TestCase
import org.apache.commons.lang3.StringUtils
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.*

/**
 * This test is not run as part of the build, because the class name ends with "Tests" instead of "Test".
 *
 * However, it is a very useful functional test which actually performs SFTP operations.
 */
class SftpRemoteTests : TestCase() {

    // These environment variables must be defined on your machine
    private val ENVIRONMENT_VARIABLE_HOST = "KSFTP_HOST"
    private val ENVIRONMENT_VARIABLE_PORT = "KSFTP_PORT"
    private val ENVIRONMENT_VARIABLE_USERNAME = "KSFTP_USERNAME"
    private val ENVIRONMENT_VARIABLE_PASSWORD = "KSFTP_PASSWORD"

    // Put your own values here...
    private val remoteDirectoryForUploads = "/vspfiles/photos/ksftpTestFileUploads"
    private val remoteDirectoryForMoves = "ksftpTestRemoteFileMoves"
    private val localDownloadsDirectory = "/Users/andyhuntsman/Desktop/_ksftpTestDownloads"

    private var testFiles: Array<File>? = null
    private var sftpClient: SftpClient? = null

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()

        sftpClient = SftpClient.create(createConnectionParameters())

        // Get the directory containing our test images
        val testImagesSourceDirectory = getTestFile("/testImages")
        TestCase.assertTrue("Test images directory could not be found!", testImagesSourceDirectory.isDirectory)
        testFiles = testImagesSourceDirectory.listFiles()
        TestCase.assertTrue("No test image files were found!", testFiles != null && testFiles!!.size > 0)
        KsftpLog.logDebug("Found " + testFiles!!.size + " test image files")
    }

    /**
     * Gets a file from the test resources package, or throws an exception if the test file doesn't exist.

     * @param relativeFilePath the file path, relative to "src/test/resources"
     */
    @Throws(Exception::class)
    private fun getTestFile(relativeFilePath: String): File {
        var theRelativeFilePath = relativeFilePath
        // Prepend the file path with a file separator, if needed
        if (!theRelativeFilePath.startsWith(File.separator)) {
            theRelativeFilePath = File.separator + theRelativeFilePath
        }

        val url = SftpRemoteTests::class.java.getResource(theRelativeFilePath)
        val testFile = File(url.file)
        TestCase.assertTrue("No test file exists for relative path '$theRelativeFilePath'", testFile.exists())
        return testFile
    }

    /**
     * Ensures that a directory exists for the specified path, and returns the [File],
     * or `null` if it could not be created.

     * @param directoryPath the directory path to ensure
     */
    @Throws(IOException::class)
    private fun ensureDirectory(directoryPath: String): File {
        val errorMessage = "Could not create directory for path '$directoryPath'"
        if (StringUtils.isEmpty(directoryPath)) {
            throw IOException(errorMessage)
        }

        val directory = File(directoryPath)
        if (directory.exists()) {
            if (!directory.isDirectory) {
                throw IOException("File '$directory' exists and is not a directory. Unable to create directory.")
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory) {
                    throw IOException("Unable to create directory '$directory'")
                }
            }
        }

        if (!directory.isDirectory) {
            throw IOException(errorMessage)
        }
        return directory
    }

    /**
     * Creates new connection parameters.
     */
    private fun createConnectionParameters(): SftpConnectionParameters {
        return SftpConnectionParametersBuilder.newInstance().createConnectionParameters()
                .withHostFromEnvironmentVariable(ENVIRONMENT_VARIABLE_HOST)
                .withPortFromEnvironmentVariable(ENVIRONMENT_VARIABLE_PORT)
                .withUsernameFromEnvironmentVariable(ENVIRONMENT_VARIABLE_USERNAME)
                .withPasswordFromEnvironmentVariable(ENVIRONMENT_VARIABLE_PASSWORD)
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testAllSftpOperations() {
        doUploads()
        doBatchDownloads()
        doBatchRenames()
        doRemoteFileDeletes()
    }

    /*@Test
    @Throws(Exception::class)
    fun testBatchUpload() {
        val remoteFilePaths = ArrayList<String>()
        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectoryForUploads + File.separator + testFile.name
            filePairs.add(FilePair(testFile.path, remoteFilePath))
            remoteFilePaths.add(remoteFilePath)
        }

        TestCase.assertTrue("Files were not uploaded!", client!!.upload(filePairs, 2, (60*5*testFiles!!.size)))
    }

    @Test
    @Throws(Exception::class)
    fun testBatchUploadTimeout() {
        val remoteFilePaths = ArrayList<String>()
        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectoryForUploads + File.separator + testFile.name
            filePairs.add(FilePair(testFile.path, remoteFilePath))
            remoteFilePaths.add(remoteFilePath)
        }

        assertFailsWith(UploadTimeoutException::class, "Batch upload timed out with an unexpected exception") {
            client!!.upload(filePairs, 2, 5)
        }
    }*/

    @Throws(Exception::class)
    private fun doUploads() {
        val remoteFilePaths = ArrayList<String>()
        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectoryForUploads + File.separator + testFile.name
            filePairs.add(FilePair(testFile.path, remoteFilePath))
            remoteFilePaths.add(remoteFilePath)
        }

        TestCase.assertTrue("Files were not uploaded!", sftpClient!!.upload(filePairs, 120*testFiles!!.size))
        TestCase.assertTrue("Files don't exist on server!", sftpClient!!.checkFiles(remoteFilePaths))
    }

    @Throws(Exception::class)
    private fun doBatchDownloads() {
        ensureDirectory(localDownloadsDirectory)
        val testDownloadsDirectory = File(localDownloadsDirectory)
        TestCase.assertTrue("Could not create test directory for downloads: '$localDownloadsDirectory'", testDownloadsDirectory.isDirectory)

        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectoryForUploads + File.separator + testFile.name
            val localPath = testDownloadsDirectory.path + File.separator + testFile.name
            filePairs.add(FilePair(localPath, remoteFilePath))
        }

        // Download
        TestCase.assertTrue("Files were not downloaded!", sftpClient!!.download(filePairs))

        // Verify
        for (filePair in filePairs) {
            val downloadedFile = File(filePair.sourceFilePath)
            TestCase.assertTrue("File was not downloaded!", downloadedFile.isFile)
        }
    }

    @Throws(Exception::class)
    private fun doBatchRenames() {
        val remoteFilePaths = ArrayList<String>()
        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val oldRemotePath = remoteDirectoryForUploads + File.separator + testFile.name
            val newRemotePath = remoteDirectoryForUploads + File.separator + remoteDirectoryForMoves + File.separator + testFile.name
            filePairs.add(FilePair(oldRemotePath, newRemotePath))
            remoteFilePaths.add(newRemotePath)
        }

        // Batch rename
        TestCase.assertTrue("Files were not renamed!", sftpClient!!.rename(filePairs))

        // Verify
        TestCase.assertTrue("Files don't exist on server!", sftpClient!!.checkFiles(remoteFilePaths))
    }

    @Throws(Exception::class)
    private fun doRemoteFileDeletes() {
        val remoteFilePath = remoteDirectoryForUploads + File.separator + remoteDirectoryForMoves + File.separator + testFiles!![2].name
        TestCase.assertTrue("File was not deleted!", sftpClient!!.delete(remoteFilePath))
        TestCase.assertFalse("Files were not actually deleted from the server!", sftpClient!!.checkFile(remoteFilePath))

        TestCase.assertTrue("Remote directory was not deleted: '$remoteDirectoryForUploads'", sftpClient!!.delete(remoteDirectoryForUploads))
        TestCase.assertFalse("Remote directory was not actually deleted from the server: '$remoteDirectoryForUploads'", sftpClient!!.checkFile(remoteDirectoryForUploads))
    }
}