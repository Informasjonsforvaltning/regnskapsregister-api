package no.regnskap.slack;

import com.hubspot.algebra.Result;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Slack {
    private static Logger LOGGER = LoggerFactory.getLogger(Slack.class);

    public static final String PRODFEIL_CHANNEL = "#prodfeil";


    public static ChatPostMessageResponse postMessage(final String token, final String channel, final String message) {
        LOGGER.info("token = (" + token.length() + "), " + token.substring(0, 5));
        if (token==null || token.isEmpty()) {
            return null;
        }

        Result<ChatPostMessageResponse, SlackError> postResult = BasicRuntimeConfig.getClient(token).postMessage(
                ChatPostMessageParams.builder()
                    .setText(message)
                    .setChannelId(channel)
                    .build()
            ).join();

        return postResult.unwrapOrElseThrow();
    }

}
