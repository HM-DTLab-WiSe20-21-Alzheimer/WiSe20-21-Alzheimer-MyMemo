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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christof Huber
 * @version 18.12.20
 */
public class CreateReminderIntentHandlerTest {

    private HandlerInput handlerInput;
    private AlexaReminderApiHandler alexaReminderApiHandler;
    private CreateReminderIntentHandler sut;

    @Before
    public void setUp() {
        AttributesManager attributesManager = mock(AttributesManager.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(attributesManager.getSessionAttributes()).thenReturn(sessionAttributes);

        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        ReminderController reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        alexaReminderApiHandler = mock(AlexaReminderApiHandler.class);
        when(alexaReminderApiHandler.checkPermission()).thenReturn(Optional.empty());
        when(reminderController.getReminderApiHandler()).thenReturn(alexaReminderApiHandler);
        sut = new CreateReminderIntentHandler(reminderController);
    }

    @Test
    public void canHandle_CreateReminderIntent_OK() {
        // arrange
        IntentRequest createReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("CreateReminderIntent")
                            .build())
            .build();

        // act
        boolean result = sut.canHandle(handlerInput, createReminderIntent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_CreateReminderIntent_NOK() {
        // arrange
        IntentRequest createReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("GNAMPF")
                            .build())
            .build();

        // act
        boolean result = sut.canHandle(handlerInput, createReminderIntent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_noSlots_OK() {
        // arrange
        IntentRequest createReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("CreateReminderIntent")
                            .build())
            .build();
        Response wantedResponse = new ResponseBuilder().withSpeech("Willst du noch eine Beschreibung hinzufuegen?")
            .withShouldEndSession(false)
            .build()
            .orElseThrow();

        // act
        Optional<Response> result = sut.handle(handlerInput, createReminderIntent);

        // assert
        assertThat(handlerInput.getAttributesManager().getSessionAttributes()).isEmpty();
        assertThat(result).isPresent()
            .contains(wantedResponse);
    }

    @Test
    public void handle_withSlots_OK() {
        // arrange
        IntentRequest createReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("CreateReminderIntent")
                            .putSlotsItem("subject", Slot.builder()
                                .withName("subject")
                                .withValue("GNAMPF")
                                .build())
                            .build())
            .build();

        // act
        Optional<Response> result = sut.handle(handlerInput, createReminderIntent);

        // assert
        assertThat(handlerInput.getAttributesManager().getSessionAttributes()).isNotEmpty();
        assertThat(handlerInput.getAttributesManager().getSessionAttributes()).containsKey("subject")
            .containsValue("GNAMPF");
        assertThat(result).isPresent();
    }

    @Test
    public void handle_noPermissions() {
        // arrange
        IntentRequest createReminderIntent = IntentRequest.builder().build();
        Optional<Response> testResponse = new ResponseBuilder().build();
        when(alexaReminderApiHandler.checkPermission()).thenReturn(testResponse);

        // act
        Optional<Response> result = sut.handle(handlerInput, createReminderIntent);

        // assert
        verify(handlerInput, never()).getAttributesManager();
        assertThat(result).isPresent()
            .isSameAs(testResponse);
    }
}