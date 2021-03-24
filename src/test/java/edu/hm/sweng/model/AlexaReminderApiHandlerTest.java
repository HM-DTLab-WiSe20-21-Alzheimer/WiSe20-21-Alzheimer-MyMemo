package edu.hm.sweng.model;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.Device;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Permissions;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.User;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.model.services.ServiceClientFactory;
import com.amazon.ask.model.services.reminderManagement.ReminderManagementServiceClient;
import com.amazon.ask.model.services.reminderManagement.ReminderRequest;
import com.amazon.ask.model.services.reminderManagement.ReminderResponse;
import com.amazon.ask.model.services.ups.UpsServiceClient;
import com.amazon.ask.model.ui.AskForPermissionsConsentCard;
import edu.hm.sweng.model.dto.MyMemoReminder;
import edu.hm.sweng.model.dto.RecurrenceUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christof Huber
 * @version 02.01.21
 */
public class AlexaReminderApiHandlerTest {

    private UpsServiceClient upsServiceClient;
    private ReminderManagementServiceClient reminderManagementServiceClient;
    private HandlerInput handlerInputMock;
    private AlexaReminderApiHandler sut;

    @Before
    public void setUp() {
        ServiceClientFactory serviceClientFactory = mock(ServiceClientFactory.class);
        reminderManagementServiceClient = mock(ReminderManagementServiceClient.class);
        upsServiceClient = mock(UpsServiceClient.class);

        when(serviceClientFactory.getReminderManagementService()).thenReturn(reminderManagementServiceClient);
        when(serviceClientFactory.getUpsService()).thenReturn(upsServiceClient);

        handlerInputMock = mock(HandlerInput.class);

        when(handlerInputMock.getServiceClientFactory()).thenReturn(serviceClientFactory);
    }

    @Test
    public void checkPermission_isOK() {
        // arrange
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withContext(Context.builder()
                             .withSystem(SystemState.builder()
                                             .withUser(User.builder()
                                                           .withPermissions(Permissions.builder()
                                                                                .withConsentToken("my consent")
                                                                                .build())
                                                           .build())
                                             .build())
                             .build())
            .build();
        when(handlerInputMock.getRequestEnvelope()).thenReturn(requestEnvelope);

        sut = new AlexaReminderApiHandler(handlerInputMock);

        // act
        Optional<Response> response = sut.checkPermission();

        // assert
        assertThat(response).isEmpty();
    }

    @Test
    public void checkPermission_noConsentToken() {
        // arrange
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withContext(Context.builder()
                             .withSystem(SystemState.builder()
                                             .withUser(User.builder()
                                                           .withPermissions(Permissions.builder()
                                                                                .build())
                                                           .build())
                                             .build())
                             .build())
            .build();
        when(handlerInputMock.getRequestEnvelope()).thenReturn(requestEnvelope);

        sut = new AlexaReminderApiHandler(handlerInputMock);

        // act
        Optional<Response> response = sut.checkPermission();

        // assert
        assertThat(response).isPresent();

        assertThat(response.get().getOutputSpeech().toString()).contains("Erinnerungen", "Erlaubnis");
        assertThat(response.get().getCard()).hasFieldOrPropertyWithValue("type", "AskForPermissionsConsent")
            .hasFieldOrProperty("permissions")
            .isInstanceOf(AskForPermissionsConsentCard.class);
        assertThat(((AskForPermissionsConsentCard) response.get().getCard()).getPermissions())
            .first()
            .isEqualTo("alexa::alerts:reminders:skill:readwrite");
    }

    @Test
    public void checkPermission_noPermissions() {
        // arrange
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withContext(Context.builder()
                             .withSystem(SystemState.builder()
                                             .withUser(User.builder()
                                                           .build())
                                             .build())
                             .build())
            .build();
        when(handlerInputMock.getRequestEnvelope()).thenReturn(requestEnvelope);

        sut = new AlexaReminderApiHandler(handlerInputMock);

        // act
        Optional<Response> response = sut.checkPermission();

        // assert
        assertThat(response).isPresent();

        assertThat(response.get().getOutputSpeech().toString()).contains("Erinnerungen", "Erlaubnis");
        assertThat(response.get().getCard()).hasFieldOrPropertyWithValue("type", "AskForPermissionsConsent")
            .hasFieldOrProperty("permissions")
            .isInstanceOf(AskForPermissionsConsentCard.class);
        assertThat(((AskForPermissionsConsentCard) response.get().getCard()).getPermissions())
            .first()
            .isEqualTo("alexa::alerts:reminders:skill:readwrite");
    }

    @Test
    public void createReminder_OK() {
        // arrange
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withContext(Context.builder()
                             .withSystem(SystemState.builder()
                                             .withDevice(Device.builder()
                                                             .withDeviceId("someDeviceId")
                                                             .build())
                                             .build())
                             .build())
            .withRequest(IntentRequest.builder()
                             .withLocale("de_DE")
                             .build())
            .build();
        when(handlerInputMock.getRequestEnvelope()).thenReturn(requestEnvelope);
        when(upsServiceClient.getSystemTimeZone("someDeviceId")).thenReturn("Wakanda");
        when(reminderManagementServiceClient.createReminder(any(ReminderRequest.class))).thenReturn(ReminderResponse.builder()
                                                                                                        .withAlertToken("ALAAARM!")
                                                                                                        .build());

        sut = new AlexaReminderApiHandler(handlerInputMock);
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now();
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "testSubject", "GNAMPF", null);

        // act
        String actual = sut.createReminder(testReminder);

        // assert
        assertThat(actual).isEqualTo("ALAAARM!");
        verify(upsServiceClient).getSystemTimeZone("someDeviceId");
        ArgumentCaptor<ReminderRequest> captor = ArgumentCaptor.forClass(ReminderRequest.class);
        verify(reminderManagementServiceClient).createReminder(captor.capture());

        ReminderRequest actualRequest = captor.getValue();
        assertThat(actualRequest.getTrigger())
            .hasFieldOrPropertyWithValue("scheduledTime", LocalDateTime.of(testDate, testTime))
            .hasFieldOrPropertyWithValue("timeZoneId", "Wakanda");
        assertThat(actualRequest.getAlertInfo().getSpokenInfo().getContent())
            .hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("text", "testSubject");
    }

    @Test
    public void createReminder_Recurrence_OK() {
        // arrange
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withContext(Context.builder()
                             .withSystem(SystemState.builder()
                                             .withDevice(Device.builder()
                                                             .withDeviceId("someDeviceId")
                                                             .build())
                                             .build())
                             .build())
            .withRequest(IntentRequest.builder()
                             .withLocale("de_DE")
                             .build())
            .build();
        when(handlerInputMock.getRequestEnvelope()).thenReturn(requestEnvelope);
        when(upsServiceClient.getSystemTimeZone("someDeviceId")).thenReturn("Wakanda");
        when(reminderManagementServiceClient.createReminder(any(ReminderRequest.class))).thenReturn(ReminderResponse.builder()
                                                                                                        .withAlertToken("ALAAARM!")
                                                                                                        .build());

        sut = new AlexaReminderApiHandler(handlerInputMock);
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now();
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "testSubject", "GNAMPF", null);
        testReminder.setRecurring(true);
        testReminder.setRecurrenceUnit(RecurrenceUnit.DAY);
        testReminder.setRecurrenceAmount(1);

        // act
        String actual = sut.createReminder(testReminder);

        // assert
        assertThat(actual).isEqualTo("ALAAARM!");
        verify(upsServiceClient).getSystemTimeZone("someDeviceId");
        ArgumentCaptor<ReminderRequest> captor = ArgumentCaptor.forClass(ReminderRequest.class);
        verify(reminderManagementServiceClient).createReminder(captor.capture());

        ReminderRequest actualRequest = captor.getValue();
        assertThat(actualRequest.getTrigger())
            .hasFieldOrPropertyWithValue("scheduledTime", LocalDateTime.of(testDate, testTime))
            .hasFieldOrPropertyWithValue("timeZoneId", "Wakanda");
        assertThat(actualRequest.getTrigger().getRecurrence().getRecurrenceRules()).isNotNull();
        assertThat(actualRequest.getAlertInfo().getSpokenInfo().getContent())
            .hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("text", "testSubject");
    }

    @Test
    public void deleteReminder_OK() {
        // arrange
        sut = new AlexaReminderApiHandler(handlerInputMock);

        // act
        sut.deleteReminder("ALAAARM!");

        // assert
        verify(reminderManagementServiceClient).deleteReminder("ALAAARM!");
    }

    @Test
    public void updateReminder_OK() {
        // arrange
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withContext(Context.builder()
                             .withSystem(SystemState.builder()
                                             .withDevice(Device.builder()
                                                             .withDeviceId("someDeviceId")
                                                             .build())
                                             .build())
                             .build())
            .withRequest(IntentRequest.builder()
                             .withLocale("de_DE")
                             .build())
            .build();
        when(handlerInputMock.getRequestEnvelope()).thenReturn(requestEnvelope);
        when(upsServiceClient.getSystemTimeZone("someDeviceId")).thenReturn("Wakanda");


        sut = new AlexaReminderApiHandler(handlerInputMock);
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now();
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "testSubject", "GNAMPF", "ALAAARM!");

        // act
        sut.updateReminder(testReminder);

        // assert
        verify(upsServiceClient).getSystemTimeZone("someDeviceId");
        ArgumentCaptor<ReminderRequest> captor = ArgumentCaptor.forClass(ReminderRequest.class);
        verify(reminderManagementServiceClient).updateReminder(eq("ALAAARM!"), captor.capture());

        ReminderRequest actualRequest = captor.getValue();
        assertThat(actualRequest.getTrigger())
            .hasFieldOrPropertyWithValue("scheduledTime", LocalDateTime.of(testDate, testTime))
            .hasFieldOrPropertyWithValue("timeZoneId", "Wakanda");
        assertThat(actualRequest.getAlertInfo().getSpokenInfo().getContent())
            .hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("text", "testSubject");
    }

    @Test
    public void getDeviceTimeZone() {
        // arrange
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withContext(Context.builder()
                             .withSystem(SystemState.builder()
                                             .withDevice(Device.builder()
                                                             .withDeviceId("someDeviceId")
                                                             .build())
                                             .build())
                             .build())
            .withRequest(IntentRequest.builder()
                             .withLocale("de_DE")
                             .build())
            .build();
        sut = new AlexaReminderApiHandler(handlerInputMock);
        //act
        sut.getDeviceTimeZone(requestEnvelope);
        //assert
        verify(upsServiceClient).getSystemTimeZone("someDeviceId");
    }
}