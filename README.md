# Overview
A simple SFTP framework written in Kotlin 1.0.3 (fully interoperable with Java), which wraps the [JCraft JSch library](http://www.jcraft.com/jsch).

This framework provides convenient APIs for:
* Uploading and downloading files to/from an SFTP server
    * Uploads can easily be divided into batches by specifying a method parameter, to support SFTP servers which limit the number of uploads for a single connection
* Renaming and moving remote files on an SFTP server
* Deleting remote files on an SFTP server
* Recursive deleting of remote directories on an SFTP server

# Quick Start
For security and convenience reasons, `ksftp` relies on environment variables on the local machine for SFTP credentials.

### Create Environment Variables: Mac 
* Launch the `Terminal` application.
* This command will either edit an existing file, or create a new one if needed: `vi ~/.bash_profile`
* By default, the `vi` editor is in "command mode". Press `i` to go into "insert" mode, so you can edit the file.
* Add these lines to the file:
```
# ksftp
export KSFTP_HOST=acmesupplies.com
export KSFTP_PORT=2222
export KSFTP_USERNAME=mySftpUsername
export KSFTP_PASSWORD=1235
```
* Press `Esc` to leave "insert mode", and resume "command mode".
* Press `:` so that you can enter a command.
    * We want to save our changes and exit, so type `x`.
    * Press `Return` to execute the command.
* To verify your changes from the terminal: `cat ~/.bash_profile`
    * You should see all of your environment variables.

### Create Environment Variables: Windows
* [Instructions for all Windows versions](http://www.computerhope.com/issues/ch000549.htm)

## Run the Unit Test to Verify SFTP Connectivity
Once your environment variables are set, you can run the `com.ajhuntsman.ksftp.SftpRemoteTests` class as a JUnit test from your workspace.

This test class orchestrates several basic operations, including renaming and deleting files on the SFTP server.

# Sample Usage
Here a couple of methods to get started:
```
/**
 * Creates a new {@link SftpConnectionParameters} object.
 */
private static SftpConnectionParameters createSftpConnectionParameters() {
    return SftpConnectionParametersBuilder.Factory.newInstance().createConnectionParameters()
        .withHost(System.getenv().get("KSFTP_HOST"))
        .withPort(Integer.parseInt(System.getenv().get("KSFTP_PORT")))
        .withUsername(System.getenv().get("KSFTP_USERNAME"))
        .withPassword(System.getenv().get("KSFTP_PASSWORD").getBytes())
        .create();
}

/**
 * Uploads a test file to the SFTP server.
 */
private boolean testUpload() throws Exception {
    return SftpClient.Factory
        .create(createSftpConnectionParameters())
        .upload("~/localTestFiles/test.txt", "/newRemoteDirectory/test.txt", 60));
}
```

## Logging
The internal logging is on by default, but can be silenced via: `KsftpLog.enableDisableLogging(false)`
