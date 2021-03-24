package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import edu.hm.sweng.model.ReminderController;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecurringReminderIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public RecurringReminderIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("RecurringReminderIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        final String frequency = intentRequest.getIntent().getSlots().get("frequency").getValue();
        Period period = Period.parse(frequency).normalized();

        int timePeriod = period.getDays();

        Optional<Response> response = reminderController.withHandlerInput(input).getReminderApiHandler().checkPermission();

        if (timePeriod <= 0) {
            response = input.getResponseBuilder().withSpeech("Bitte gib einen zulaessigen Zeitabstand an.").build();
        }

        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        Map<String, Object> taskData = new HashMap<>();
        slots.forEach((key, value) -> taskData.put(key, value.getValue()));
        taskData.put("recurring", true);
        taskData.put("recurringAmount", timePeriod);
        return response.or(() -> {
            reminderController.withHandlerInput(input)
                .saveReminder(taskData, Optional.empty());
            return input.getResponseBuilder()
                .withSpeech("Du wirst regelmaessig erinnert.")
                .withShouldEndSession(true)
                .build();
        });
    }
}
