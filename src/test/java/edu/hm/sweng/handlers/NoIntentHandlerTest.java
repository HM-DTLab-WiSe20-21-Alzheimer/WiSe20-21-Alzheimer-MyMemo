package edu.hm.sweng.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.ReminderController;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christof Huber
 * @version 21.12.20
 */
public class NoIntentHandlerTest {

    private ReminderController reminderController;
    private NoIntentHandler sut;

    @Before
    public void setUp() {
        reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        sut = new NoIntentHandler(reminderController);
    }

    @Test
    public void canHandle_CreateReminderIntent_OK() {
        // arrange
        IntentRequest noIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("AMAZON.NoIntent")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        // act
        boolean result = sut.canHandle(handlerInput, noIntent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_CreateReminderIntent_NOK() {
        // arrange
        IntentRequest noIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("GNAMPF")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        // act
        boolean result = sut.canHandle(handlerInput, noIntent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_OK() {
        // arrange
        IntentRequest noIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("AMAZON.NoIntent")
                            .build())
            .build();
        Map<String, Object> testMap = new HashMap<>();
        AttributesManager attributesManager = mock(AttributesManager.class);
        when(attributesManager.getSessionAttributes()).thenReturn(testMap);

        HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);

        // act
        Optional<Response> result = sut.handle(handlerInput, noIntent);

        // assert
        verify(reminderController).saveReminder(testMap, Optional.empty());
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Ok, dann wars das.");
    }
}