package edu.hm.sweng.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.ReminderController;
import edu.hm.sweng.model.dto.MyMemoReminder;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static org.apache.logging.log4j.LogManager.getLogger;

public class AddDescriptionIntentHandler implements IntentRequestHandler {

    private static final Logger LOGGER = getLogger(AddDescriptionIntentHandler.class);
    private final ReminderController reminderController;

    public AddDescriptionIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("AddDescriptionIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        LOGGER.info("Got new reminder with description");

        AttributesManager attributesManager = input.getAttributesManager();
        String description = intentRequest.getIntent().getSlots().get("description").getValue();

        MyMemoReminder myMemoReminder = reminderController
            .withHandlerInput(input)
            .saveReminder(attributesManager.getSessionAttributes(), Optional.of(description));

        LOGGER.info("Saved new reminder");
        return input.getResponseBuilder()
            .withSpeech("Du wirst am " + myMemoReminder.getDate() + " an " + myMemoReminder.getSubject() + " erinnert werden.")
            .build();
    }

}