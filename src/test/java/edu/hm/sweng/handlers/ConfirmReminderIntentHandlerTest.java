package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.ReminderController;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfirmReminderIntentHandlerTest {

    private ReminderController reminderController;
    private ConfirmReminderIntentHandler sut;

    @Before
    public void setUp() throws Exception {
        reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        sut = new ConfirmReminderIntentHandler(reminderController);
    }


    @Test
    public void test_canHandle() {
        IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent
                            .builder()
                            .withName("ConfirmReminderIntent")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        boolean canHandle = sut.canHandle(handlerInput, intent);

        assertTrue(canHandle);
    }

    @Test
    public void test_cantHandle() {
        IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent
                            .builder()
                            .withName("other")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        boolean canHandle = sut.canHandle(handlerInput, intent);

        assertFalse(canHandle);
    }

    @Test
    public void handle_found() {
        IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent
                            .builder()
                            .withName("other")
                            .putSlotsItem("subject", Slot.builder().withValue("ABCABC").build())
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);
        when(reminderController.confirmReminder("ABCABC")).thenReturn(true);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        Optional<Response> expected = new ResponseBuilder()
            .withSpeech("Ich habe die Erinnerung fuer dich bestaetigt.")
            .build();

        Optional<Response> response = sut.handle(handlerInput, intent);

        assertTrue(response.isPresent());
        assertEquals(expected, response);
    }

    @Test
    public void handle_notFound() {
        IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent
                            .builder()
                            .withName("other")
                            .putSlotsItem("subject", Slot.builder().withValue("ABCABC").build())
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);
        when(reminderController.confirmReminder("ABCABC")).thenReturn(false);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        Optional<Response> expected = new ResponseBuilder()
            .withSpeech("Ich konnte fuer dich keine passende Erinnerung finden.")
            .build();

        Optional<Response> response = sut.handle(handlerInput, intent);

        assertTrue(response.isPresent());
        assertEquals(expected, response);
    }
}