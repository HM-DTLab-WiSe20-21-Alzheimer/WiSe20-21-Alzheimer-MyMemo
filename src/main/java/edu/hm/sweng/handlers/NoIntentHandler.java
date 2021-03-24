package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.ReminderController;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static org.apache.logging.log4j.LogManager.getLogger;

public class NoIntentHandler implements IntentRequestHandler {

    private static final Logger LOGGER = getLogger(NoIntentHandler.class);

    private final ReminderController reminderController;

    public NoIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("AMAZON.NoIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        LOGGER.info("Got new reminder without description");

        reminderController.withHandlerInput(input)
            .saveReminder(input.getAttributesManager().getSessionAttributes(), Optional.empty());

        LOGGER.info("Saved new reminder");
        return input.getResponseBuilder()
            .withSpeech("Ok, dann wars das.")
            .build();
    }

}