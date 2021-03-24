package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.ReminderController;

import java.util.Optional;

public class ConfirmReminderIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public ConfirmReminderIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("ConfirmReminderIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {

        final String subject = intentRequest.getIntent().getSlots().get("subject").getValue();
        Optional<Response> response;
        if (reminderController.withHandlerInput(input).confirmReminder(subject)) {
            response = input.getResponseBuilder()
                .withSpeech("Ich habe die Erinnerung fuer dich bestaetigt.")
                .build();
        } else {
            response = input.getResponseBuilder()
                .withSpeech("Ich konnte fuer dich keine passende Erinnerung finden.")
                .build();
        }

        return response;
    }
}
