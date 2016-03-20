package com.ajhuntsman.batchsftp

import junit.framework.TestCase
import org.apache.commons.lang3.StringUtils
import org.junit.Before
import java.io.File
import java.io.IOException
import java.util.*

/**
 * This test is not run as part of the build, because the class name ends with "Tests" instead of "Test".
 *
 * However, it is a very useful functional test which actually performs SFTP operations.
 */
class SftpRemoteTests : TestCase() {

    // Put your own values here...
    private val ENVIRONMENT_VARIABLE_HOST = "mySftpServer.com"
    private val ENVIRONMENT_VARIABLE_PORT = "2222"
    private val ENVIRONMENT_VARIABLE_USERNAME = "mySftpUsername"
    private val ENVIRONMENT_VARIABLE_PASSWORD = "mySftpPassword"
    private val remoteDirectory = "/myRemoteTestDirectory/testPhotos"
    private val localDownloadsDirectory = "/Users/johndoe/Desktop/_testDownloads"

    private var testFiles: Array<File>? = null
    private var client: Client? = null

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()

        client = Client.create(createConnectionParameters())

        // Get the directory containing our test images
        val testImagesSourceDirectory = getTestFile("/testImages")
        TestCase.assertTrue("Test images directory could not be found!", testImagesSourceDirectory.isDirectory)
        testFiles = testImagesSourceDirectory.listFiles()
        TestCase.assertTrue("No test image files were found!", testFiles != null && testFiles!!.size > 0)
        SftpLog.logFine("Found " + testFiles!!.size + " test image files")
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
    private fun createConnectionParameters(): ConnectionParameters {
        return ConnectionParametersBuilder.create().createConnectionParameters()
                .withHostFromEnvironmentVariable(ENVIRONMENT_VARIABLE_HOST)
                .withPortFromEnvironmentVariable(ENVIRONMENT_VARIABLE_PORT)
                .withUsernameFromEnvironmentVariable(ENVIRONMENT_VARIABLE_USERNAME)
                .withPasswordFromEnvironmentVariable(ENVIRONMENT_VARIABLE_PASSWORD)
                .create()
    }

    @Throws(Exception::class)
    fun testAllSftpOperations() {
        doBatchUploads()
        doBatchDownloads()
        doBatchRenames()
        doRemoteFileDeletes()
    }

    @Throws(Exception::class)
    private fun doBatchUploads() {
        val remoteFilePaths = ArrayList<String>()
        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectory + File.separator + testFile.name
            filePairs.add(FilePair(testFile.path, remoteFilePath))
            remoteFilePaths.add(remoteFilePath)
        }

        TestCase.assertTrue("Files were not uploaded!", client!!.upload(filePairs))
        TestCase.assertTrue("Files don't exist on server!", client!!.checkFiles(remoteFilePaths))
    }

    @Throws(Exception::class)
    private fun doBatchDownloads() {
        ensureDirectory(localDownloadsDirectory)
        val testDownloadsDirectory = File(localDownloadsDirectory)
        TestCase.assertTrue("Could not create test directory for downloads: '$localDownloadsDirectory'", testDownloadsDirectory.isDirectory)

        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectory + File.separator + testFile.name
            val localPath = testDownloadsDirectory.path + File.separator + testFile.name
            filePairs.add(FilePair(localPath, remoteFilePath))
        }

        // Download
        TestCase.assertTrue("Files were not downloaded!", client!!.download(filePairs))

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
            val oldRemotePath = remoteDirectory + File.separator + testFile.name
            val newRemotePath = remoteDirectory + File.separator + "testRemoteFileMoves" + File.separator + testFile.name
            filePairs.add(FilePair(oldRemotePath, newRemotePath))
            remoteFilePaths.add(newRemotePath)
        }

        // Batch rename
        TestCase.assertTrue("Files were not renamed!", client!!.rename(filePairs))

        // Verify
        TestCase.assertTrue("Files don't exist on server!", client!!.checkFiles(remoteFilePaths))
    }

    @Throws(Exception::class)
    private fun doRemoteFileDeletes() {
        val remoteFilePath = remoteDirectory + File.separator + "testRemoteFileMoves" + File.separator + testFiles!![2].name
        TestCase.assertTrue("File was not deleted!", client!!.delete(remoteFilePath))

        TestCase.assertFalse("Files were not actually deleted from the server!", client!!.checkFile(remoteFilePath))

        //TestCase.assertTrue("Remote directory was not deleted!", client!!.delete(remoteDirectory))
    }
}