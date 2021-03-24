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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class CheckoutRemindersIntentHandlerTest {

    private final Map<String, Object> persistentAttributes = new HashMap<>();
    private HandlerInput handlerInput;
    private CheckoutRemindersIntentHandler sut;
    private AttributesManager attributesManager;
    private MockedStatic<MyMemoSharingData> settings;

    @Before
    public void setUp() {
        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        sut = new CheckoutRemindersIntentHandler();
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
    public void canHandle_checkoutRemindersIntent_OK() {
        // arrange
        final var checkoutRemindersIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("CheckoutRemindersIntent")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, checkoutRemindersIntent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_checkoutRemindersIntent_NOK() {
        // arrange
        final var checkoutRemindersIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("WOODOO")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, checkoutRemindersIntent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_withSlotsOK() {
        // arrange
        final var checkoutRemindersIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("CheckoutRemindersIntent")
                            .putSlotsItem("name", Slot.builder()
                                .withName("name")
                                .withValue("Sitka")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, checkoutRemindersIntent);
        final var slots = checkoutRemindersIntent.getIntent().getSlots();

        // assert
        assertThat(slots).isNotEmpty()
            .containsKey("name");
        assertThat(slots.get("name").getValue()).isEqualTo("Sitka");
        assertThat(result).isPresent();
    }

    @Test
    public void handle_handlingOK() {
        // arrange
        final List<String> reminder = List.of("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}");
        when(MyMemoSharingData.loadFromDB("MAGIC KEY")).thenReturn(reminder);
        persistentAttributes.put("trusted reminders", Map.of("Sitka", "MAGIC KEY"));

        final var checkoutRemindersIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("CheckoutRemindersIntent")
                            .putSlotsItem("name", Slot.builder()
                                .withName("name")
                                .withValue("Sitka")
                                .build())
                            .build())
            .build();

        // act
        final var response = sut.handle(handlerInput, checkoutRemindersIntent);

        // assert
        assertThat(response).isPresent();
        assertThat(response.get().getOutputSpeech().toString()).contains("GNAMPF");
    }

    @Test
    public void handle_handlingNOK() {
        // arrange
        final List<String> reminder = List.of("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}");
        when(MyMemoSharingData.loadFromDB("MAGIC KEY")).thenReturn(reminder);
        persistentAttributes.put("trusted reminders", Map.of("Definitely not Sitka", "MAGIC KEY"));

        final var checkoutRemindersIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("CheckoutRemindersIntent")
                            .putSlotsItem("name", Slot.builder()
                                .withName("name")
                                .withValue("Sitka")
                                .build())
                            .build())
            .build();

        // act
        final var response = sut.handle(handlerInput, checkoutRemindersIntent);

        // assert
        assertThat(response).isPresent();
        assertFalse(response.get().getOutputSpeech().toString().contains("GNAMPF"));
    }


}
