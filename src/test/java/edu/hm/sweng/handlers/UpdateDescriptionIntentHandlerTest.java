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

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateDescriptionIntentHandlerTest {

    private ReminderController reminderController;
    private UpdateDescriptionIntentHandler sut;

    @Before
    public void setUp() throws Exception {
        reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        sut = new UpdateDescriptionIntentHandler(reminderController);
    }

    @Test
    public void canHandle_UpdateReminderIntent_OK() {
        final IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("UpdateDescriptionIntent")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);
        final boolean result = sut.canHandle(handlerInput, intent);
        assertTrue(result);
    }

    @Test
    public void canHandle_UpdateReminderIntent_NOK() {
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
    public void handle_Unambiguous() {
        //arrange
        when(reminderController.updateWithData(anyMap())).thenReturn(true);
        final IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("UpdateDescriptionIntent")
                            .putSlotsItem("date", Slot.builder().withValue("28.10.2020").build())
                            .putSlotsItem("newDescription", Slot.builder().withValue("ABCD").build())
                            .build())
            .build();
        final AttributesManager attributesManager = mock(AttributesManager.class);
        final HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);

        //act
        final var result = sut.handle(handlerInput, intent);

        //assert
        assertTrue(result.isPresent());
        assertTrue(result.get().getOutputSpeech().toString().contains("Die Beschreibung wurde erfolgreich geaendert."));
        verify(reminderController).updateWithData(Map.of("date", "28.10.2020", "newDescription", "ABCD"));
    }

    @Test
    public void handle_Ambiguous() {
        //arrange
        when(reminderController.updateWithData(anyMap())).thenReturn(false);
        final IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("UpdateDescriptionIntent")
                            .putSlotsItem("date", Slot.builder().withValue("28.10.2020").build())
                            .putSlotsItem("newDescription", Slot.builder().withValue("ABCD").build())
                            .build())
            .build();
        final AttributesManager attributesManager = mock(AttributesManager.class);
        final HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);

        //act
        final var result = sut.handle(handlerInput, intent);

        //assert
        assertTrue(result.isPresent());
        assertTrue(result.get().getOutputSpeech().toString().contains("Es wurden mehrere Erinnerungen gefunden, welche deinen Angaben entsprechen."));
        verify(reminderController).updateWithData(any());
    }
}