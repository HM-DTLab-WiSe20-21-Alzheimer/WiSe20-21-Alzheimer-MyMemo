package edu.hm.sweng.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.ReminderController;
import edu.hm.sweng.model.dto.MyMemoReminder;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christof Huber
 * @version 21.12.20
 */
public class AddDescriptionIntentHandlerTest {

    private ReminderController reminderController;
    private AddDescriptionIntentHandler sut;

    @Before
    public void setUp() {
        reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        sut = new AddDescriptionIntentHandler(reminderController);
    }

    @Test
    public void canHandle_CreateReminderIntent_OK() {
        // arrange
        IntentRequest addDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("AddDescriptionIntent")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        // act
        boolean result = sut.canHandle(handlerInput, addDescriptionIntent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_CreateReminderIntent_NOK() {
        // arrange
        IntentRequest addDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("GNAMPF")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        // act
        boolean result = sut.canHandle(handlerInput, addDescriptionIntent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_withSlots_OK() {
        // arrange
        IntentRequest addDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("AddDescriptionIntent")
                            .putSlotsItem("description", Slot.builder()
                                .withName("description")
                                .withValue("GNAMPF")
                                .build())
                            .build())
            .build();
        Map<String, Object> testMap = new HashMap<>();
        AttributesManager attributesManager = mock(AttributesManager.class);
        when(attributesManager.getSessionAttributes()).thenReturn(testMap);

        HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);

        when(reminderController.saveReminder(anyMap(), eq(Optional.of("GNAMPF")))).thenReturn(new MyMemoReminder());

        // act
        Optional<Response> result = sut.handle(handlerInput, addDescriptionIntent);

        // assert
        verify(reminderController).saveReminder(testMap, Optional.of("GNAMPF"));
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Du wirst", "erinnert werden.");
    }
}