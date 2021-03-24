package edu.hm.sweng.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.MyMemoSharingData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ToggleSharingRemindersIntentHandlerTest {

    private HandlerInput handlerInput;
    private ToggleSharingRemindersIntentHandler sut;
    private AttributesManager attributesManager;
    private final Map<String, Object> persistentAttributes = new HashMap<>();
    private MockedStatic<MyMemoSharingData> settings;

    @Before
    public void setUp() {
        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        sut = new ToggleSharingRemindersIntentHandler();
        attributesManager = mock(AttributesManager.class);
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        attributesManager.setPersistentAttributes(persistentAttributes);
        when(attributesManager.getPersistentAttributes()).thenReturn(persistentAttributes);
        settings = mockStatic(MyMemoSharingData.class);
    }

    @After
    public void close() {
        settings.close();
    }

    @Test
    public void canHandle_toggleSharingRemindersIntent_OK() {
        // arrange
        final var toggleSharingReminder = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ToggleSharingRemindersIntent")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, toggleSharingReminder);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_toggleSharingRemindersIntent_NOK() {
        // arrange
        final var toggleSharingReminder = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("WOODOO")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, toggleSharingReminder);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_handlingDeactivate() {
        // arrange
        final var toggleSharingReminder = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ToggleSharingRemindersIntent")
                            .putSlotsItem("toggle", Slot.builder()
                                .withName("name")
                                .withValue("deaktiviere")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, toggleSharingReminder);

        // arrange
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Deine reminder werden nicht mehr geteilt");
    }

    @Test
    public void handle_handlingActivate() {
        // arrange
        when(MyMemoSharingData.generateTempKey(attributesManager)).thenReturn("1234567890");
        final var toggleSharingReminder = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ToggleSharingRemindersIntent")
                            .putSlotsItem("toggle", Slot.builder()
                                .withName("name")
                                .withValue("aktiviere")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, toggleSharingReminder);
        final var key = (String) persistentAttributes.get("private key");
        // arrange
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("1, 2, 3, 4, 5, 6, 7, 8, 9, 0");
        assertThat(key).isNotNull()
            .hasSize(25);
    }
}
