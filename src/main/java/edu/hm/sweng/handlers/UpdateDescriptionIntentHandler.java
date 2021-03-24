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

public class UpdateDescriptionIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public UpdateDescriptionIntentHandler(final ReminderController reminderController) {
        this.reminderController = reminderController;
    }


    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("UpdateDescriptionIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        final Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        ReminderController controller = reminderController.withHandlerInput(input);

        final Map<String, Object> taskData = new HashMap<>();
        slots.forEach((key, value) -> taskData.put(key, value.getValue()));

        String response;
        if (controller.updateWithData(taskData)) {
            response = "Die Beschreibung wurde erfolgreich geaendert.";
        } else {
            response = "Es wurden mehrere Erinnerungen gefunden, welche deinen Angaben entsprechen."
                + System.lineSeparator()
                + "Bitte versuche es mit mehr Informationen erneut."
                + System.lineSeparator()
                + "Sage hierzu: Aendere die Beschreibung von einer Erinnerung am Datum um Uhrzeit";
        }
        return input.getResponseBuilder()
            .withSpeech(response)
            .withShouldEndSession(true)
            .build();
    }
}

