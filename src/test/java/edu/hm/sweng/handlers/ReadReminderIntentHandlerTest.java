package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.ReminderController;
import edu.hm.sweng.model.ReminderRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadReminderIntentHandlerTest {

    private HandlerInput handlerInput;
    private ReadReminderIntentHandler sut;
    private ReminderRepository reminderRepository;

    @Before
    public void setUp() throws Exception {
        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        final var reminderController = mock(ReminderController.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);

        reminderRepository = mock(ReminderRepository.class);
        when(reminderController.withHandlerInput(handlerInput).getRepository()).thenReturn(reminderRepository);

        sut = new ReadReminderIntentHandler(reminderController);
    }

    @Test
    public void canHandle_OK() {
        // arrange
        final var readReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadReminderIntent")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, readReminderIntent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_NOK() {
        // arrange
        final var readReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("WOODOO")
                            .build())
            .build();

        // act
        final var result = sut.canHandle(handlerInput, readReminderIntent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_withSlots_OK() {
        // arrange
        final var readReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadReminderIntent")
                            .putSlotsItem("firstDate", Slot.builder()
                                .withName("firstDate")
                                .withValue("WOODOO")
                                .build())
                            .putSlotsItem("secondDate", Slot.builder()
                                .withName("secondDate")
                                .withValue("FOO")
                                .build())
                            .putSlotsItem("timeframe", Slot.builder()
                                .withName("timeframe")
                                .withValue("MOOO")
                                .build())
                            .build())
            .build();

        // act
        final var result = sut.handle(handlerInput, readReminderIntent);
        final var slots = readReminderIntent.getIntent().getSlots();
        // assert
        assertThat(slots).isNotEmpty()
            .containsKey("timeframe");
        assertThat(slots.get("timeframe").getValue()).isEqualTo("MOOO");
        assertThat(result).isPresent();
    }

    @Test
    public void handle_reminderListEmpty() {
        //arrange
        final var readReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadReminderIntent")
                            .putSlotsItem("firstDate", Slot.builder()
                                .withName("firstDate")
                                .withValue(null)
                                .build())
                            .putSlotsItem("secondDate", Slot.builder()
                                .withName("secondDate")
                                .withValue(null)
                                .build())
                            .putSlotsItem("timeframe", Slot.builder()
                                .withName("timeframe")
                                .withValue("morgen")
                                .build())
                            .build())
            .build();
        //act
        final var result = sut.handle(handlerInput, readReminderIntent);
        //assert
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("keine Reminder");
    }

    @Test
    public void handle_reminderListNotEmpty() {
        //arrange
        final var testList = List.of("MICKY");
        when(reminderRepository.getReminderSubjectsForTimeframe(null, null, null)).thenReturn(testList);

        final var readReminderIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadReminderIntent")
                            .putSlotsItem("firstDate", Slot.builder()
                                .withName("firstDate")
                                .withValue(null)
                                .build())
                            .putSlotsItem("secondDate", Slot.builder()
                                .withName("secondDate")
                                .withValue(null)
                                .build())
                            .putSlotsItem("timeframe", Slot.builder()
                                .withName("timeframe")
                                .withValue(null)
                                .build())
                            .build())
            .build();
        //act
        final var result = sut.handle(handlerInput, readReminderIntent);
        //assert
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("MICKY");
    }
}