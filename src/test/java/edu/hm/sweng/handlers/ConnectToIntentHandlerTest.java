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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ConnectToIntentHandlerTest {

    private HandlerInput handlerInput;
    private ConnectToIntentHandler sut;
    private AttributesManager attributesManager;
    private final Map<String, Object> persistentAttributes = new HashMap<>();
    private MockedStatic<MyMemoSharingData> settings;

    @Before
    public void setUp() {
        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        sut = new ConnectToIntentHandler();
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
    public void canHandle_connectToIntent_OK() {
        // arrange
        final var connectToIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ConnectToIntent")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, connectToIntent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_connectToIntent_NOK() {
        // arrange
        final var connectToIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("WOODOO")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, connectToIntent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_handlingOK1() {
        // arrange
        when(MyMemoSharingData.loadFromDB("0123456789")).thenReturn(List.of(LocalDate.now().toString(),
                                                                            LocalTime.now().toString(),
                                                                            "Donald Duck"));
        final var connectToIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ConnectToIntent")
                            .putSlotsItem("name", Slot.builder()
                                .withName("name")
                                .withValue("Sitka")
                                .build())
                            .putSlotsItem("key", Slot.builder()
                                .withName("key")
                                .withValue("null eins zwei drei vier f\u00fcnf sechs sieben acht neun")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, connectToIntent);

        // arrange
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Sitka");
    }

    @Test
    public void handle_handlingOK2() {
        // arrange
        when(MyMemoSharingData.loadFromDB("0123456789")).thenReturn(List.of(LocalDate.now().toString(),
                                                                            LocalTime.now().toString(),
                                                                            "Donald Duck"));
        persistentAttributes.put("trusted reminders", new HashMap<String, String>());
        final var connectToIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ConnectToIntent")
                            .putSlotsItem("name", Slot.builder()
                                .withName("name")
                                .withValue("Sitka")
                                .build())
                            .putSlotsItem("key", Slot.builder()
                                .withName("key")
                                .withValue("null eins zwei drei vier f\u00fcnf sechs sieben acht neun")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, connectToIntent);

        // arrange
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Sitka");
    }

    //to cover all branches
    @Test
    public void handle_handlingNOK1() {
        // arrange
        when(MyMemoSharingData.loadFromDB("0123456789")).thenReturn(List.of(LocalDate.now().plusDays(1).toString(),
                                                                            LocalTime.now().toString(),
                                                                            "Donald Duck"));
        final var connectToIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ConnectToIntent")
                            .putSlotsItem("name", Slot.builder()
                                .withName("name")
                                .withValue("Sitka")
                                .build())
                            .putSlotsItem("key", Slot.builder()
                                .withName("key")
                                .withValue("null eins zwei drei vier f\u00fcnf sechs sieben acht neun")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, connectToIntent);

        // arrange
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("ungueltig");
    }

    @Test
    public void handle_handlingNOK2() {
        // arrange
        when(MyMemoSharingData.loadFromDB("0123456789")).thenReturn(List.of(LocalDate.now().toString(),
                                                                            LocalTime.now().minusMinutes(6).toString(),
                                                                            "Donald Duck"));
        final var connectToIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ConnectToIntent")
                            .putSlotsItem("name", Slot.builder()
                                .withName("name")
                                .withValue("Sitka")
                                .build())
                            .putSlotsItem("key", Slot.builder()
                                .withName("key")
                                .withValue("null eins zwei drei vier f\u00fcnf sechs sieben acht neun")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, connectToIntent);

        // arrange
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("ungueltig");
    }


}
