package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import edu.hm.sweng.model.ReminderController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReadReminderIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public ReadReminderIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("ReadReminderIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        reminderController.withHandlerInput(input).syncReminders();
        final Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        final String firstDate = slots.get("firstDate").getValue();
        final String secondDate = slots.get("secondDate").getValue();
        final String timeframe = slots.get("timeframe").getValue();
        final List<String> reminders = reminderController
            .withHandlerInput(input)
            .getRepository()
            .getReminderSubjectsForTimeframe(firstDate, secondDate, timeframe);

        String response;
        if (reminders.isEmpty()) {
            response = "Ich konnte keine Reminder finden";
        } else {
            response = "Ich habe folgende Reminder fuer dich gefunden: " +
                String.join(", ", reminders);
        }
        return input.getResponseBuilder()
            .withSpeech(response)
            .withShouldEndSession(true)
            .build();
    }
}
