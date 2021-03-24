package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import edu.hm.sweng.model.ReminderController;

import java.util.Map;
import java.util.Optional;

public class CreateReminderIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public CreateReminderIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("CreateReminderIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        Optional<Response> response = reminderController.withHandlerInput(input).getReminderApiHandler().checkPermission();
        return response.or(() -> {
            Map<String, Slot> slots = intentRequest.getIntent().getSlots();
            slots.forEach((key, value) -> input.getAttributesManager().getSessionAttributes().put(key, value.getValue()));
            return input.getResponseBuilder()
                .withSpeech("Willst du noch eine Beschreibung hinzufuegen?")
                .withShouldEndSession(false)
                .build();
        });
    }
}