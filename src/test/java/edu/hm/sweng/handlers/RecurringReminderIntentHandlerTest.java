package edu.hm.sweng.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.AlexaReminderApiHandler;
import edu.hm.sweng.model.ReminderController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecurringReminderIntentHandlerTest {

    private HandlerInput handlerInput;
    private AlexaReminderApiHandler alexaReminderApiHandler;
    private RecurringReminderIntentHandler sut;
    private ReminderController reminderController;

    @Before
    public void setUp() {
        AttributesManager attributesManager = mock(AttributesManager.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(attributesManager.getSessionAttributes()).thenReturn(sessionAttributes);

        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        this.reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        alexaReminderApiHandler = mock(AlexaReminderApiHandler.class);
        when(alexaReminderApiHandler.checkPermission()).thenReturn(Optional.empty());
        when(reminderController.getReminderApiHandler()).thenReturn(alexaReminderApiHandler);
        sut = new RecurringReminderIntentHandler(reminderController);
    }

    @Test
    public void canHandle_RecurringReminderIntent_OK() {
        // arrange
        IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("RecurringReminderIntent")
                            .build())
            .build();

        // act
        boolean result = sut.canHandle(handlerInput, intent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_RecurringReminderIntent_NOK() {
        // arrange
        IntentRequest intent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("GNAMPF")
                            .build())
            .build();

        // act
        boolean result = sut.canHandle(handlerInput, intent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_Weeks_OK() {
        // arrange
        IntentRequest recurrenceIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("RecurringReminderIntent")
                            .putSlotsItem("frequency", Slot.builder().withValue("P14D").build())
                            .build())
            .build();
        Response wantedResponse = new ResponseBuilder().withSpeech("Du wirst regelmaessig erinnert.")
            .withShouldEndSession(true)
            .build()
            .orElseThrow();

        // act
        Optional<Response> result = sut.handle(handlerInput, recurrenceIntent);

        // assert
        ArgumentCaptor<Map<String, Object>> taskData = ArgumentCaptor.forClass(Map.class);
        verify(reminderController).saveReminder(taskData.capture(), any());
        assertThat(taskData.getValue().get("recurring")).isNotNull();
        assertThat(taskData.getValue()).containsEntry("recurringAmount", 14);
        assertThat(result).get()
            .isEqualTo(wantedResponse);
    }

    @Test
    public void handle_Invalid() {
        // arrange
        IntentRequest recurrenceIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("RecurringReminderIntent")
                            .putSlotsItem("frequency", Slot.builder().withValue("P0D").build())
                            .build())
            .build();

        Optional<Response> want = new ResponseBuilder().withSpeech("Bitte gib einen zulaessigen Zeitabstand an.").build();
        // act
        Optional<Response> result = sut.handle(handlerInput, recurrenceIntent);

        assertThat(result).contains(want.get());
    }

    @Test
    public void handle_Days_OK() {
        // arrange
        IntentRequest recurrenceIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("RecurringReminderIntent")
                            .putSlotsItem("frequency", Slot.builder().withValue("P3D").build())
                            .build())
            .build();
        Response wantedResponse = new ResponseBuilder().withSpeech("Du wirst regelmaessig erinnert.")
            .withShouldEndSession(true)
            .build()
            .orElseThrow();

        // act
        Optional<Response> result = sut.handle(handlerInput, recurrenceIntent);

        // assert
        ArgumentCaptor<Map<String, Object>> taskData = ArgumentCaptor.forClass(Map.class);
        verify(reminderController).saveReminder(taskData.capture(), any());
        assertThat(taskData.getValue().get("recurring")).isNotNull();
        assertThat(taskData.getValue()).containsEntry("recurringAmount", 3);
        assertThat(result).get()
            .isEqualTo(wantedResponse);
    }
}