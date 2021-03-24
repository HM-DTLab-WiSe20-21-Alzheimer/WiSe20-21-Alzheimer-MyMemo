package edu.hm.sweng.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.ReminderController;
import edu.hm.sweng.model.ReminderRepository;
import edu.hm.sweng.model.dto.MyMemoReminder;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christof Huber
 * @version 26.12.20
 */
public class ReadDescriptionIntentHandlerTest {

    private ReminderRepository reminderRepository;
    private ReminderController reminderController;
    private ReadDescriptionIntentHandler sut;

    @Before
    public void setUp() {
        reminderController = mock(ReminderController.class);
        reminderRepository = mock(ReminderRepository.class);
        when(reminderController.withHandlerInput(any(HandlerInput.class))).thenReturn(reminderController);
        when(reminderController.getRepository()).thenReturn(reminderRepository);
        sut = new ReadDescriptionIntentHandler(reminderController);
    }

    @Test
    public void canHandle_ReadDescriptionIntent_OK() {
        // arrange
        IntentRequest readDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadDescriptionIntent")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        // act
        boolean result = sut.canHandle(handlerInput, readDescriptionIntent);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    public void canHandle_ReadDescriptionIntent_NOK() {
        // arrange
        IntentRequest readDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("GNAMPF")
                            .build())
            .build();
        HandlerInput handlerInput = mock(HandlerInput.class);

        // act
        boolean result = sut.canHandle(handlerInput, readDescriptionIntent);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void handle_noRemindersInDb() {
        // arrange
        IntentRequest readDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadDescriptionIntent")
                            .putSlotsItem("subject", Slot.builder()
                                .withName("subject")
                                .withValue("test")
                                .build())
                            .build())
            .build();
        AttributesManager attributesManager = mock(AttributesManager.class);
        HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        when(reminderController.noReminders()).thenReturn(true);

        // act
        Optional<Response> result = sut.handle(handlerInput, readDescriptionIntent);

        // assert
        verify(reminderController).noReminders();
        verify(reminderRepository, never()).getReminderBySubject(anyString());
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Du hast noch keine Erinnerung gespeichert");
    }

    @Test
    public void handle_noReminderForSubjectFound() {
        // arrange
        IntentRequest readDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadDescriptionIntent")
                            .putSlotsItem("subject", Slot.builder()
                                .withName("subject")
                                .withValue("test")
                                .build())
                            .build())
            .build();
        AttributesManager attributesManager = mock(AttributesManager.class);
        HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        // act
        Optional<Response> result = sut.handle(handlerInput, readDescriptionIntent);

        // assert
        verify(reminderController).noReminders();
        verify(reminderRepository).getReminderBySubject("test");
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Leider konnte ich deine Erinnerung: ", "nicht finden");
    }

    @Test
    public void handle_reminderWithDescriptionFound() {
        // arrange
        IntentRequest readDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadDescriptionIntent")
                            .putSlotsItem("subject", Slot.builder()
                                .withName("subject")
                                .withValue("test")
                                .build())
                            .build())
            .build();
        AttributesManager attributesManager = mock(AttributesManager.class);
        HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        when(reminderRepository.getReminderBySubject("test"))
            .thenReturn(Optional.of(new MyMemoReminder(LocalDate.now(), LocalTime.now(), "test", "GNAMPF", null)));

        // act
        Optional<Response> result = sut.handle(handlerInput, readDescriptionIntent);

        // assert
        verify(reminderController).noReminders();
        verify(reminderRepository).getReminderBySubject("test");
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Zu", "gibt es folgende Beschreibung", "Die Erinnerung ist festgelegt auf den");
    }

    @Test
    public void handle_reminderWithoutDescriptionFound() {
        // arrange
        IntentRequest readDescriptionIntent = IntentRequest.builder()
            .withIntent(Intent.builder()
                            .withName("ReadDescriptionIntent")
                            .putSlotsItem("subject", Slot.builder()
                                .withName("subject")
                                .withValue("test")
                                .build())
                            .build())
            .build();
        AttributesManager attributesManager = mock(AttributesManager.class);
        HandlerInput handlerInput = mock(HandlerInput.class);
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        when(reminderRepository.getReminderBySubject("test"))
            .thenReturn(Optional.of(new MyMemoReminder(LocalDate.now(), LocalTime.now(), "test", null, null)));

        // act
        Optional<Response> result = sut.handle(handlerInput, readDescriptionIntent);

        // assert
        verify(reminderController).noReminders();
        verify(reminderRepository).getReminderBySubject("test");
        assertThat(result).isPresent();
        assertThat(result.get().getOutputSpeech().toString()).contains("Zu", "ist keine genauere Beschreibung gegeben", "Die Erinnerung ist festgelegt auf den");
    }
}