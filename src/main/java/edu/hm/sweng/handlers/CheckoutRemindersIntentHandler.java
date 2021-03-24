package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.TrustedReminderRepositories;
import edu.hm.sweng.model.dto.MyMemoReminder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CheckoutRemindersIntentHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("CheckoutRemindersIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        final var trustedRepos = new TrustedReminderRepositories(input.getAttributesManager());
        final String name = intentRequest.getIntent().getSlots().get("name").getValue();
        final List<MyMemoReminder> reminders = trustedRepos.get(name);
        final String response;
        if (reminders == null) {
            response = "Ich konnte keine Reminder fuer diese Person finden";
        } else {
            final List<String> subjects = reminders.stream()
                .map(MyMemoReminder::getSubject)
                .collect(Collectors.toList());

            response = String.format("Ich habe folgende reminder fuer %s gefunden: %s",
                                     name, String.join(", ", subjects));
        }
        return input.getResponseBuilder()
            .withSpeech(response)
            .withShouldEndSession(true)
            .build();
    }
}
