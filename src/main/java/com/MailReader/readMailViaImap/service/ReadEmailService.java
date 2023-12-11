package com.MailReader.readMailViaImap.service;

import com.MailReader.readMailViaImap.entity.AttachmentInformation;
import com.MailReader.readMailViaImap.entity.EmailInformation;
import com.MailReader.readMailViaImap.entity.SessionParameter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

@Service
@RequiredArgsConstructor
public class ReadEmailService {
    public static List<EmailInformation> readEmails(SessionParameter sessionParameter) {

        List<EmailInformation> emailInfoList = new ArrayList<>();

        String host = sessionParameter.getHostName();
        String username = sessionParameter.getUserName();
        String password = sessionParameter.getPassword();

        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.auth.plain.disable", "false");
        properties.put("mail.imap.auth.mechanisms", "PLAIN");
        properties.put("mail.debug", "true");
        properties.put("mail.imaps.ssl.trust", "*");

        Session session = Session.getInstance(properties);

        try {
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            for (Message message : messages) {
                EmailInformation emailInfo = new EmailInformation();

                // set subject
                emailInfo.setSubject(message.getSubject());

                // set fromAddress
                Address[] fromAddresses = message.getFrom();
                if (fromAddresses != null && fromAddresses.length > 0) {
                    String fromEmail = ((InternetAddress) fromAddresses[0]).getAddress();
                    emailInfo.setFrom(fromEmail);
                }

                // set sentDate
                emailInfo.setSentDate((message.getSentDate()));

                // set content
                String content = contentCleaner(extractContentFromMessage(message));
                emailInfo.setContent(content);

                // set attachments
                emailInfo.setAttachmentInformation(processAttachments(message));

                // set messageNumber
                emailInfo.setMessageNumber(message.getMessageNumber());

                emailInfoList.add(emailInfo);
            }

            inbox.close(true);
            store.close();

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }

        return emailInfoList;
    }

    private static String extractContentFromMessage(Message message) throws MessagingException, IOException {
        Object content = message.getContent();

        if (content instanceof Multipart) {
            return extractTextFromMimeMultipart((Multipart) content);
        } else {
            return content.toString();
        }
    }

    private static String contentCleaner(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);

        Elements paragraphs = document.select("div.WordSection1");

        StringBuilder result = new StringBuilder();

        for (Element paragraph : paragraphs) {
            String paragraphText = paragraph.text();
            result.append(paragraphText);
        }
        if(result.isEmpty()) {
            String plainText = document.text();

            int legalDisclaimerIndex = plainText.indexOf("YASAL UYARI");
            if (legalDisclaimerIndex != -1) {
                return plainText.substring(0, legalDisclaimerIndex);
            } else {
                return plainText;
            }
        }
        return result.toString();
    }
    private static String extractTextFromMimeMultipart(Multipart multipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            // Check if the part is text/plain or text/html
            if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof Multipart) {
                // Recursively handle nested MimeMultipart
                result.append(extractTextFromMimeMultipart((Multipart) bodyPart.getContent()));
            }
        }

        return result.toString();
    }

    private static List<AttachmentInformation> processAttachments(Message message)
            throws MessagingException, IOException {
        List<AttachmentInformation> attachments = new ArrayList<>();

        Object content = message.getContent();
        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String fileName = bodyPart.getFileName();
                    byte[] attachmentData = readAttachmentData(bodyPart);

                    // İndirme bağlantısını oluştur ve AttachmentInformation nesnesine ekle
                    String downloadLink = generateDownloadLink(fileName, attachmentData);
                    attachments.add(new AttachmentInformation(fileName, downloadLink));
                }
            }
        }

        return attachments;
    }

    private static String generateDownloadLink(String fileName, byte[] attachmentData) {
        return "http://example.com/download?filename=" + fileName;
    }


    private static byte[] readAttachmentData(BodyPart bodyPart) throws IOException, MessagingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bodyPart.writeTo(outputStream);
        return outputStream.toByteArray();
    }
}