package us.careydevelopment.util.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.careydevelopment.util.api.google.CredentialUtil;
import us.careydevelopment.util.api.google.config.GoogleApiConfig;
import us.careydevelopment.util.api.google.exception.GoogleApiException;
import us.careydevelopment.util.gmail.constants.GmailConstants;
import us.careydevelopment.util.gmail.exception.GmailApiException;

import java.io.IOException;
import java.util.List;

public class GmailService {

    private static final Logger LOG = LoggerFactory.getLogger(GmailService.class);

    private Gmail gmail;
    
    public GmailService(final String userId) {
        setup(userId);
    }

    private void setup(final String userId) {
        final GoogleApiConfig config = GoogleApiConfig.getInstance();

        final HttpTransport transport = config.getTransport();
        final JsonFactory factory = config.getJsonFactory();
        final String applicationName = config.getApplicationName();

        final Credential credential = CredentialUtil.getCredential(userId);

        gmail = new Gmail.Builder(transport, factory, credential)
                .setApplicationName(applicationName)
                .build();
    }

    public List<Message> getMessageList() throws GoogleApiException {
        try {
            Gmail.Users.Messages messages = gmail
                                            .users()
                                            .messages();

            Gmail.Users.Messages.List messageList = messages
                    .list(GmailConstants.CURRENT_USER)
                    .setQ(GmailConstants.INBOX_QUERY)
                    .setMaxResults(GmailConstants.INBOX_EMAIL_COUNT);

            ListMessagesResponse rsp = messageList.execute();
            List<Message> list = rsp.getMessages();

            return list;
        } catch (TokenResponseException ie) {
            LOG.error(ie.getDetails().getErrorDescription(), ie);
            throw new GmailApiException(ie.getMessage());
        } catch (IOException ie) {
            LOG.error("Problem retrieving emails!", ie);
            throw new GmailApiException(ie.getMessage());
        }
    }

    public Message getSingleEmailMessageById(String id, boolean lightweight) {
        try {
            Message retrievedMessage = gmail
                                        .users()
                                        .messages()
                                        .get(GmailConstants.CURRENT_USER, id)
                                        .setFormat(GmailConstants.FULL_FORMAT)
                                        .execute();

            return retrievedMessage;
        } catch (IOException ie) {
            LOG.error("Problem retrieving individual message!");
            throw new GmailApiException(ie.getMessage());
        }
    }

    public Message sendEmail(Message message) throws IOException {
        Message sentMessage = gmail
                                .users()
                                .messages()
                                .send(GmailConstants.CURRENT_USER, message)
                                .execute();

        return sentMessage;
    }
}
