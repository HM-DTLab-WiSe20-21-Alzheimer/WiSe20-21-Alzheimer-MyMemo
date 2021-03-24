package edu.hm.sweng.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.ReminderController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteReminderIntentHandlerTest {

    private ReminderController reminderController;
    private DeleteReminderIntentHandler sut;


    @Before
    public void setUp() throws Exception {
        reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        sut = new DeleteReminderIntentHandler(reminderController);
    }

    @Test
    public void canHandle_DeleteReminderIntent_OK() {
        final IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("DeleteReminderIntent")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);
        final boolean result = sut.canHandle(handlerInput, intent);
        assertTrue(result);
    }

    @Test
    public void canHandle_DeleteReminderIntent_NOK() {
        final IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("OtherIntent")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);
        final boolean result = sut.canHandle(handlerInput, intent);
        assertFalse(result);
    }

    @Test
    public void handle() {

        final AttributesManager attributesManager = mock(AttributesManager.class);
        final HandlerInput input = mock(HandlerInput.class);
        when(input.getAttributesManager()).thenReturn(attributesManager);
        when(input.getResponseBuilder()).thenReturn(new ResponseBuilder());
        final IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .putSlotsItem("subject", Slot.builder()
                                .withValue("buy food")
                                .build())
                            .build())
            .build();

        sut.handle(input, intent);

        verify(reminderController).deleteReminder("buy food");
    }
}