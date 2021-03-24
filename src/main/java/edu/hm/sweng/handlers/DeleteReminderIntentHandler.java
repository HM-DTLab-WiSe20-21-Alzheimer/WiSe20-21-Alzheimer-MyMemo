package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.ReminderController;

import java.util.Optional;

public class DeleteReminderIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public DeleteReminderIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("DeleteReminderIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {

        final String subject = intentRequest.getIntent().getSlots().get("subject").getValue();
        reminderController.withHandlerInput(input)
            .deleteReminder(subject);

        return input.getResponseBuilder()
            .withSpeech("Ich habe die Erinnerung " + subject + " fuer dich geloescht.")
            .build();
    }
}
