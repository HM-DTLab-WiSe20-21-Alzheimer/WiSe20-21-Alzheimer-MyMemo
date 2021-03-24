package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import edu.hm.sweng.model.ReminderController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpdateReminderIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public UpdateReminderIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("UpdateReminderIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        final Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        final Map<String, Object> taskData = new HashMap<>();
        slots.forEach((key, value) -> taskData.put(key, value.getValue()));

        String response;
        if (reminderController.withHandlerInput(input).updateWithData(taskData)) {
            response = "Deine Erinnerung wurde erfolgreich geaendert.";
        } else {
            response = "Es wurden mehrere Erinnerungen gefunden, welche deinen Angaben entsprechen."
                + System.lineSeparator()
                + "Bitte versuche es mit mehr Informationen erneut.";
        }
        return input.getResponseBuilder()
            .withSpeech(response)
            .withShouldEndSession(true)
            .build();
    }
}
