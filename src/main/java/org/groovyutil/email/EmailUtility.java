package org.groovyutil.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

public class EmailUtility {

    private static final Logger logger = LoggerFactory.getLogger(EmailMessage.class);

    private static Folder imapFolder;
    private static Store imapStore;
    private static Session session;
    private static Transport smtpTransport;

    public static void OpenImapConnection(String server, String user, String password) throws Exception {
        Properties props = System.getProperties();


        props.setProperty("mail.imapStore.protocol","imap");

        session = Session.getDefaultInstance(props, null);
        imapStore = session.getStore("imaps");
        imapStore.connect(server, user, password);
    }
    
    public static void OpenImapConnection() throws Exception {
        OpenImapConnection("application.properties");
    }
    
    public static void OpenImapConnection(String propertyFilename) throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = EmailUtility.class.getResourceAsStream("/" + propertyFilename);
        properties.load(inputStream);

        String imapServer = properties.getProperty("imap.server");
        String imapUser = properties.getProperty("imap.user");
        String imapPassword = properties.getProperty("imap.password");

        OpenImapConnection(imapServer, imapUser, imapPassword);
    }
    
    public static void OpenSmtpConnection() throws Exception {
        OpenSmtpConnection("application.properties");   
    }
    
    public static void OpenSmtpConnection(String propertyFilename) throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = EmailUtility.class.getResourceAsStream("/" + propertyFilename);
        properties.load(inputStream);

        String smtpServer = properties.getProperty("smtp.server");
        String smtpUser = properties.getProperty("smtp.user");
        String smtpPassword = properties.getProperty("smtp.password");

        OpenSmtpConnection(smtpServer, smtpUser, smtpPassword);
    }

    public static void OpenSmtpConnection(String server, String user, String password) throws Exception {
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.port","587");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");

        session = Session.getDefaultInstance(props, null);

        smtpTransport = session.getTransport("smtp");
        smtpTransport.connect(server, user, password);
    }

    public static void CloseSmtpConnection() throws MessagingException {
        smtpTransport.close();
    }

    public static void SendMessage(EmailMessage emailMessage) throws MessagingException {
        smtpTransport.sendMessage(emailMessage.message, emailMessage.message.getAllRecipients());
    }

    public static EmailMessage CreateEmailMessage() {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.message = new MimeMessage(session);
        return emailMessage;
    }

    public static void OpenImapFolder(String imapFolderName) throws MessagingException {
        imapFolder = imapStore.getFolder(imapFolderName);
        imapFolder.open(Folder.READ_WRITE);
    }

    public static void CloseImapConnection() throws MessagingException {
        imapFolder.close(true);
        imapStore.close();
    }

    public static void CopyMessageToImapFolder(EmailMessage emailMessage, String folderName) throws MessagingException {
            Message[] messages = new Message[1];
            messages[0] = emailMessage.message;
            Folder archiveFolder = imapStore.getFolder(folderName);
            if (!archiveFolder.exists()) { archiveFolder.create(Folder.HOLDS_MESSAGES);}
            imapFolder.copyMessages(messages, archiveFolder);
    }

    public static EmailMessage GetFirstMessageInFolder() throws MessagingException {
        EmailMessage emailMessage = null;
        if (imapFolder.getMessageCount() > 0) {
            emailMessage = new EmailMessage();
            emailMessage.message = imapFolder.getMessage(1);
        }
        return emailMessage;
    }

    public static void DeleteMessage(EmailMessage emailMessage) throws Exception {
        emailMessage.message.setFlag(Flags.Flag.DELETED, true);
        imapFolder.expunge();
    }
}
