package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.ReminderController;
import edu.hm.sweng.model.dto.MyMemoReminder;

import java.time.LocalDate;
import java.util.Optional;


public class ReadDescriptionIntentHandler implements IntentRequestHandler {

    private final ReminderController reminderController;

    public ReadDescriptionIntentHandler(ReminderController reminderController) {
        this.reminderController = reminderController;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("ReadDescriptionIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        String subject = intentRequest.getIntent().getSlots().get("subject").getValue();
        ReminderController controller = reminderController.withHandlerInput(input);
        String speechText;
        if (controller.noReminders()) {
            speechText = "Du hast noch keine Erinnerung gespeichert.";
        } else {
            Optional<MyMemoReminder> foundReminder = controller.getRepository().getReminderBySubject(subject);
            //if there is no reminder with the subject found
            if (foundReminder.isEmpty()) {
                speechText = "Leider konnte ich deine Erinnerung: " + subject + " nicht finden.";
            } else {
                //get the reminder attributes
                MyMemoReminder reminder = foundReminder.get();
                String description = reminder.getDescription();
                LocalDate savedDate = reminder.getDate();
                String savedSubject = reminder.getSubject();
                //if there is no description give back the time
                if (description == null) {
                    speechText = "Zu \"" + savedSubject + "\" ist keine genauere Beschreibung gegeben. Die Erinnerung ist festgelegt auf den " + savedDate + " um " + foundReminder.get().getTime();
                } else {
                    //give back the description
                    speechText = "Zu \"" + savedSubject + "\" gibt es folgende Beschreibung: " + description + ". Die Erinnerung ist festgelegt auf den " + savedDate + " um " + foundReminder.get().getTime();
                }
            }
        }
        return input.getResponseBuilder()
            .withSpeech(speechText)
            .withShouldEndSession(true)
            .build();
    }


}
