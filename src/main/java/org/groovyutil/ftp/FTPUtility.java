package org.groovyutil.ftp;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

public class FTPUtility {

    public static class JschLogger implements com.jcraft.jsch.Logger {
        static java.util.Hashtable name=new java.util.Hashtable();
        static{
            name.put(new Integer(DEBUG), "DEBUG: ");
            name.put(new Integer(INFO), "INFO: ");
            name.put(new Integer(WARN), "WARN: ");
            name.put(new Integer(ERROR), "ERROR: ");
            name.put(new Integer(FATAL), "FATAL: ");
        }
        public boolean isEnabled(int level){
            return true;
        }
        public void log(int level, String message){
            System.err.print(name.get(new Integer(level)));
            System.err.println(message);
        }
    }

    private static Session session = null;
    private static String keyFile = null;
    private static ChannelSftp sftpChannel;

    public static void Connect() throws JSchException {
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        sftpChannel = (ChannelSftp) channel;
    }

    public static void DisconnectSFtpConnection() {
        sftpChannel.exit();
        session.disconnect();
    }

    public static String Pwd() throws SftpException {
        return sftpChannel.pwd();
    }

    public static void Cd(String remoteDirectory) throws SftpException {
        sftpChannel.cd(remoteDirectory);
    }

    public static void Lcd(String localDirectory) throws Exception {
        sftpChannel.lcd(localDirectory);
    }

    public static void Put(String localFile) throws SftpException {
        sftpChannel.put(localFile);
    }

    public static void Put(String localFile, String remoteFile) throws SftpException {
        sftpChannel.put(localFile, remoteFile);
    }

    public static void Get(String remoteFile) throws Exception {
        sftpChannel.get(remoteFile);
    }

    public static void Get(String remoteFile, String localFile) throws Exception {
        sftpChannel.get(remoteFile, localFile);
    }

    public static String Lpwd() {
        return sftpChannel.lpwd();
    }

    public static void OpenSFtpConnection() throws Exception {
        OpenSFtpConnection("application.properties");
    }

    public static void OpenSFtpConnection(String propertyFilename) throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = FTPUtility.class.getResourceAsStream("/" + propertyFilename);
        properties.load(inputStream);

        String ftpServer = properties.getProperty("ftp.server");
        String ftpUser = properties.getProperty("ftp.user");
        String ftpPassword = properties.getProperty("ftp.password");
        String ftpIdentityFile = properties.getProperty("ftp.identity");
        String ftpPortString = properties.getProperty("ftp.port");
        keyFile = ftpIdentityFile;

        Integer ftpPort = 22;

        if (ftpPortString != null) {
            ftpPort = new Integer(ftpPortString);
        }

        OpenSFtpConnection(ftpServer, ftpUser, ftpPassword, ftpPort);
    }

    public static void OpenSFtpConnection(String server, String user, String password, int port) throws Exception {

        JSch.setLogger(new JschLogger());

        JSch jsch = new JSch();
        if (keyFile != null) {
            jsch.addIdentity(keyFile);
        }
        session = jsch.getSession(user, server, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
        session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
        session.setConfig("compression_level", "9");


        if (keyFile == null) {
            session.setPassword(password);
        }

        Connect();
    }

    public static void FtpAddIdentity(String inputKeyFile) {
        keyFile = inputKeyFile;
    }

    public static void OpenSFtpConnection(String server, String user, String password) throws Exception {
        OpenSFtpConnection(server, user, password, 22);
    }

    public static String[] Ls(String fileSpec) throws Exception {
        Vector<ChannelSftp.LsEntry> vector = sftpChannel.ls(fileSpec);
        ArrayList<String> filenames = new ArrayList<String>();
        for (ChannelSftp.LsEntry entry : vector) {
            filenames.add(entry.getFilename());
        }

        return filenames.toArray(new String[0]);
    }

    public static void Rm(String filename) throws SftpException {
        sftpChannel.rm(filename);
    }


    public static void main(String[] args) throws Exception {
        FtpAddIdentity("<key-file-name>");
        OpenSFtpConnection("<host-ip>", "<username>", "", 22);

        Cd("/data");
        String[] files = Ls("*");
        System.out.println(Arrays.toString(files));
    }
}
