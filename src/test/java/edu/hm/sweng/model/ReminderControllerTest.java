package edu.hm.sweng.model;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.services.ServiceClientFactory;
import com.amazon.ask.model.services.reminderManagement.ReminderManagementServiceClient;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.dto.MyMemoReminder;
import edu.hm.sweng.model.dto.RecurrenceUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christof Huber
 * @version 02.01.21
 */
public class ReminderControllerTest {

    private HandlerInput handlerInput;
    private ReminderRepository reminderRepositoryMock;
    private AlexaReminderApiHandler alexaReminderApiHandlerMock;
    private ReminderController sut;

    @Before
    public void setUp() {
        handlerInput = mock(HandlerInput.class);
        ReminderController rc = ReminderController.getReminderController().withHandlerInput(handlerInput);
        sut = spy(rc);
        reminderRepositoryMock = mock(ReminderRepository.class);
        doReturn(reminderRepositoryMock).when(sut).getRepository();
        alexaReminderApiHandlerMock = mock(AlexaReminderApiHandler.class);
        doReturn(alexaReminderApiHandlerMock).when(sut).getReminderApiHandler();
    }

    @Test
    public void test_createsNewRepositoryAndApiHandler() {
        // arrange
        doCallRealMethod().when(sut).getRepository();
        ServiceClientFactory serviceClientFactory = mock(ServiceClientFactory.class);
        ReminderManagementServiceClient reminderManagementServiceClient = mock(ReminderManagementServiceClient.class);
        when(serviceClientFactory.getReminderManagementService()).thenReturn(reminderManagementServiceClient);
        when(handlerInput.getServiceClientFactory()).thenReturn(serviceClientFactory);
        doCallRealMethod().when(sut).getReminderApiHandler();

        // act
        ReminderRepository sutRepository = sut.getRepository();
        AlexaReminderApiHandler sutReminderApiHandler = sut.getReminderApiHandler();

        // assert
        assertThat(sutRepository).isExactlyInstanceOf(ReminderRepository.class);
        assertThat(sutReminderApiHandler).isExactlyInstanceOf(AlexaReminderApiHandler.class);
    }

    @Test
    public void test_saveReminder_withDescription() {
        // arrange
        Map<String, Object> testTaskData = Map.of("date", "2012-12-21", "time", "12:00", "subject", "GNAMPF");
        Optional<String> testDescription = Optional.of("Bernd");
        when(alexaReminderApiHandlerMock.createReminder(any(MyMemoReminder.class))).thenReturn("ALAAARM!");

        // act
        MyMemoReminder actual = sut.saveReminder(testTaskData, testDescription);

        // assert
        assertThat(actual).hasNoNullFieldsOrPropertiesExcept("reminderId", "recurrenceUnit")
            .hasFieldOrPropertyWithValue("subject", "GNAMPF")
            .hasFieldOrPropertyWithValue("date", LocalDate.parse("2012-12-21"))
            .hasFieldOrPropertyWithValue("time", LocalTime.parse("12:00"))
            .hasFieldOrPropertyWithValue("description", "Bernd")
            .hasFieldOrPropertyWithValue("reminderId", "ALAAARM!");

        InOrder inOrder = inOrder(alexaReminderApiHandlerMock, reminderRepositoryMock);
        inOrder.verify(alexaReminderApiHandlerMock).createReminder(any(MyMemoReminder.class));
        inOrder.verify(reminderRepositoryMock).saveReminder(actual);
    }

    @Test
    public void test_saveReminder_sanitizedInput() {
        // arrange
        Map<String, Object> testTaskData = Map.of("date", "2012-12-21", "time", "12:00", "subject", "GNAMPF mit my memo");
        Optional<String> testDescription = Optional.of("Bernd");

        // act
        MyMemoReminder actual = sut.saveReminder(testTaskData, testDescription);

        // assert
        assertThat(actual).hasFieldOrPropertyWithValue("subject", "GNAMPF");

        verify(reminderRepositoryMock).saveReminder(actual);
    }


    @Test
    public void test_saveReminder_withoutDescription() {
        // arrange
        Map<String, Object> testTaskData = Map.of("date", "2012-12-21", "time", "12:00", "subject", "GNAMPF");
        Optional<String> testDescription = Optional.empty();

        // act
        MyMemoReminder actual = sut.saveReminder(testTaskData, testDescription);

        // assert
        assertThat(actual)
            .hasFieldOrPropertyWithValue("subject", "GNAMPF")
            .hasFieldOrPropertyWithValue("date", LocalDate.parse("2012-12-21"))
            .hasFieldOrPropertyWithValue("time", LocalTime.parse("12:00"))
            .hasFieldOrPropertyWithValue("description", null);

        verify(reminderRepositoryMock).saveReminder(actual);

    }

    @Test
    public void test_deleteReminderFound() {
        // arrange
        MyMemoReminder testReminder = new MyMemoReminder();
        testReminder.setReminderId("ALAAARM!");
        when(reminderRepositoryMock.deleteReminder("GNAMPF")).thenReturn(List.of(testReminder));

        // act
        sut.deleteReminder("GNAMPF");

        // assert
        verify(reminderRepositoryMock).deleteReminder("GNAMPF");
        verify(alexaReminderApiHandlerMock).deleteReminder("ALAAARM!");
    }

    @Test
    public void test_deleteReminderNotFound() {
        // arrange
        when(reminderRepositoryMock.deleteReminder("other reminder")).thenReturn(List.of());

        // act
        sut.deleteReminder("other reminder");

        // assert
        verify(reminderRepositoryMock).deleteReminder("other reminder");
        verify(alexaReminderApiHandlerMock, never()).deleteReminder(anyString());
    }

    @Test
    public void test_updateWithData_newDateTime_oneMatch() {
        // arrange
        LocalDate testDate = LocalDate.now().minusDays(7);
        LocalTime testTime = LocalTime.now().minusHours(2);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        MyMemoReminder testReminder2 = new MyMemoReminder(testDate, testTime, "GNAMPF2", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder, testReminder2));
        Map<String, Object> testTaskData = new HashMap<>();
        testTaskData.put("subject", "GNAMPF");
        testTaskData.put("date", testDate.toString());
        testTaskData.put("time", testTime.toString());

        testTaskData.put("newDate", LocalDate.now().toString());
        testTaskData.put("newTime", LocalTime.now().toString());

        when(reminderRepositoryMock.updateReminder(any(), any(), any(), any())).thenReturn(List.of(testReminder2));

        // act
        boolean actual = sut.updateWithData(testTaskData);

        // assert
        assertThat(actual).isTrue();
        ArgumentCaptor<MyMemoReminder> captor = ArgumentCaptor.forClass(MyMemoReminder.class);
        verify(reminderRepositoryMock).updateReminder(
            captor.capture(),
            any(Optional.class),
            any(Optional.class),
            any(Optional.class));

        MyMemoReminder updatedReminder = captor.getValue();
        assertThat(updatedReminder).isSameAs(testReminder);
        verify(alexaReminderApiHandlerMock).updateReminder(testReminder2);
    }

    @Test
    public void test_updateWithData_bySubjectOnly_oneMatch() {
        // arrange
        LocalDate testDate = LocalDate.now().minusDays(7);
        LocalTime testTime = LocalTime.now().minusHours(2);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        MyMemoReminder testReminder2 = new MyMemoReminder(testDate, testTime, "GNAMPF2", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder, testReminder2));
        Map<String, Object> testTaskData = new HashMap<>();
        testTaskData.put("subject", "GNAMPF");

        testTaskData.put("newDate", LocalDate.now().toString());
        testTaskData.put("newTime", LocalTime.now().toString());

        when(reminderRepositoryMock.updateReminder(any(), any(), any(), any())).thenReturn(List.of(testReminder2));

        // act
        boolean actual = sut.updateWithData(testTaskData);

        // assert
        assertThat(actual).isTrue();
        ArgumentCaptor<MyMemoReminder> captor = ArgumentCaptor.forClass(MyMemoReminder.class);
        verify(reminderRepositoryMock).updateReminder(
            captor.capture(),
            any(Optional.class),
            any(Optional.class),
            any(Optional.class));

        MyMemoReminder updatedReminder = captor.getValue();
        assertThat(updatedReminder).isSameAs(testReminder);
        verify(alexaReminderApiHandlerMock).updateReminder(testReminder2);
    }

    @Test
    public void test_updateWithData_newDateTime_noMatch() {
        // arrange
        LocalDate testDate = LocalDate.now().minusDays(7);
        LocalTime testTime = LocalTime.now().minusHours(2);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        Map<String, Object> testTaskData = new HashMap<>();
        testTaskData.put("subject", "other");
        testTaskData.put("date", testDate.toString());
        testTaskData.put("time", testTime.toString());

        testTaskData.put("newDate", LocalDate.now().toString());
        testTaskData.put("newTime", LocalTime.now().toString());
        // act
        boolean actual = sut.updateWithData(testTaskData);

        // assert
        assertThat(actual).isFalse();

        verify(reminderRepositoryMock, never()).updateReminder(
            any(MyMemoReminder.class),
            any(Optional.class),
            any(Optional.class),
            any(Optional.class));
        verify(alexaReminderApiHandlerMock, never()).updateReminder(any(MyMemoReminder.class));
    }

    @Test
    public void test_updateWithData_newDateTime_tooManyMatches() {
        // arrange
        LocalDate testDate = LocalDate.now().minusDays(7);
        LocalTime testTime = LocalTime.now().minusHours(2);
        MyMemoReminder testReminder1 = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        MyMemoReminder testReminder2 = new MyMemoReminder(testDate, testTime, "GNAMPF", "some description", null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder1, testReminder2));
        Map<String, Object> testTaskData = new HashMap<>();
        testTaskData.put("subject", "GNAMPF");
        testTaskData.put("date", testDate.toString());
        testTaskData.put("time", testTime.toString());

        testTaskData.put("newDate", LocalDate.now().toString());
        testTaskData.put("newTime", LocalTime.now().toString());
        // act
        boolean actual = sut.updateWithData(testTaskData);

        // assert
        assertThat(actual).isFalse();

        verify(reminderRepositoryMock, never()).updateReminder(
            any(MyMemoReminder.class),
            any(Optional.class),
            any(Optional.class),
            any(Optional.class));
        verify(alexaReminderApiHandlerMock, never()).updateReminder(any(MyMemoReminder.class));
    }

    @Test
    public void test_noReminders_noReminder() {
        // arrange
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of());

        // act
        boolean actual = sut.noReminders();

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    public void test_noReminders_oneReminder() {
        // arrange
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(new MyMemoReminder()));

        // act
        boolean actual = sut.noReminders();

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    public void test_saveReminder_recurring_Week() {
        LocalDate testDate = LocalDate.now().minusDays(7);
        LocalTime testTime = LocalTime.now().minusHours(2);
        Map<String, Object> taskData = Map.of("recurring", true,
                                              "recurringAmount", 14,
                                              "subject", "GNAMPF",
                                              "date", testDate.toString(),
                                              "time", testTime.toString()
        );
        MyMemoReminder wanted = new MyMemoReminder();
        wanted.setRecurring(true);
        wanted.setRecurrenceAmount(2);
        wanted.setRecurrenceUnit(RecurrenceUnit.WEEK);
        wanted.setTime(testTime);
        wanted.setDate(testDate);
        wanted.setSubject("GNAMPF");
        sut.saveReminder(taskData, Optional.empty());

        ArgumentCaptor<MyMemoReminder> resultingReminder = ArgumentCaptor.forClass(MyMemoReminder.class);
        verify(reminderRepositoryMock).saveReminder(resultingReminder.capture());
        assertThat(resultingReminder.getValue()).isEqualTo(wanted);
    }

    @Test
    public void test_saveReminder_recurring_Day() {
        LocalDate testDate = LocalDate.now().minusDays(7);
        LocalTime testTime = LocalTime.now().minusHours(2);
        Map<String, Object> taskData = Map.of("recurring", true,
                                              "recurringAmount", 5,
                                              "subject", "GNAMPF",
                                              "date", testDate.toString(),
                                              "time", testTime.toString()
        );
        MyMemoReminder wanted = new MyMemoReminder();
        wanted.setRecurring(true);
        wanted.setRecurrenceAmount(5);
        wanted.setRecurrenceUnit(RecurrenceUnit.DAY);
        wanted.setTime(testTime);
        wanted.setDate(testDate);
        wanted.setSubject("GNAMPF");
        sut.saveReminder(taskData, Optional.empty());

        ArgumentCaptor<MyMemoReminder> resultingReminder = ArgumentCaptor.forClass(MyMemoReminder.class);
        verify(reminderRepositoryMock).saveReminder(resultingReminder.capture());
        assertThat(resultingReminder.getValue()).isEqualTo(wanted);
    }

    @Test
    public void test_syncReminders_noneFound_time() {
        //arrange
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now().plusMinutes(60);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        when(reminderRepositoryMock.isReadyToSync()).thenReturn(true);
        when(alexaReminderApiHandlerMock.checkPermission()).thenReturn(Optional.empty());
        when(alexaReminderApiHandlerMock.getDeviceTimeZone(any())).thenReturn(TimeZone.getDefault().getDisplayName());
        //act
        sut.syncReminders();
        //assert
        verify(alexaReminderApiHandlerMock, times(0)).createReminder(any());
    }

    @Test
    public void test_syncReminders_found() {
        //arrange
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now().minusMinutes(60);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        when(alexaReminderApiHandlerMock.checkPermission()).thenReturn(Optional.empty());
        when(alexaReminderApiHandlerMock.getDeviceTimeZone(any())).thenReturn(Calendar.getInstance().getTimeZone().getID());
        when(reminderRepositoryMock.isReadyToSync()).thenReturn(true);
        ArgumentCaptor<MyMemoReminder> changedReminder = ArgumentCaptor.forClass(MyMemoReminder.class);
        //act
        sut.syncReminders();
        //assert
        verify(alexaReminderApiHandlerMock, times(1)).createReminder(changedReminder.capture());
        assertThat(changedReminder.getValue().getTime()).isAfter(LocalTime.now(Calendar.getInstance().getTimeZone().toZoneId()));
    }

    @Test
    public void test_syncReminders_notReady() {
        //arrange
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now().minusMinutes(60);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        when(alexaReminderApiHandlerMock.checkPermission()).thenReturn(Optional.empty());
        when(alexaReminderApiHandlerMock.getDeviceTimeZone(any())).thenReturn(Calendar.getInstance().getTimeZone().getID());
        when(reminderRepositoryMock.isReadyToSync()).thenReturn(false);
        //act
        sut.syncReminders();
        //assert
        verify(alexaReminderApiHandlerMock, times(0)).createReminder(any());
    }

    @Test
    public void test_syncReminders_notReady2() {
        //arrange
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now().minusMinutes(60);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        when(alexaReminderApiHandlerMock.checkPermission()).thenReturn(new ResponseBuilder().build());
        when(alexaReminderApiHandlerMock.getDeviceTimeZone(any())).thenReturn(Calendar.getInstance().getTimeZone().getID());
        when(reminderRepositoryMock.isReadyToSync()).thenReturn(true);
        //act
        sut.syncReminders();
        //assert
        verify(alexaReminderApiHandlerMock, times(0)).createReminder(any());
    }

    @Test
    public void test_syncReminders_noneFound_recurring() {
        //arrange
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now().minusMinutes(60);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        testReminder.setRecurring(true);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        when(reminderRepositoryMock.isReadyToSync()).thenReturn(true);
        when(alexaReminderApiHandlerMock.checkPermission()).thenReturn(Optional.empty());
        when(alexaReminderApiHandlerMock.getDeviceTimeZone(any())).thenReturn(TimeZone.getDefault().getDisplayName());
        //act
        sut.syncReminders();
        //assert
        verify(alexaReminderApiHandlerMock, times(0)).createReminder(any());
    }

    @Test
    public void test_syncReminders_noneFound_date() {
        //arrange
        LocalDate testDate = LocalDate.now().plusDays(2);
        LocalTime testTime = LocalTime.now().minusMinutes(60);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        when(reminderRepositoryMock.isReadyToSync()).thenReturn(true);
        when(alexaReminderApiHandlerMock.checkPermission()).thenReturn(Optional.empty());
        when(alexaReminderApiHandlerMock.getDeviceTimeZone(any())).thenReturn(TimeZone.getDefault().getDisplayName());
        //act
        sut.syncReminders();
        //assert
        verify(alexaReminderApiHandlerMock, times(0)).createReminder(any());
    }

    @Test
    public void test_syncReminders_found_date() {
        //arrange
        LocalDate testDate = LocalDate.now().minusDays(2);
        LocalTime testTime = LocalTime.now().plusMinutes(60);
        MyMemoReminder testReminder = new MyMemoReminder(testDate, testTime, "GNAMPF", null, null);
        when(reminderRepositoryMock.getRemindersFromDb()).thenReturn(List.of(testReminder));
        when(alexaReminderApiHandlerMock.checkPermission()).thenReturn(Optional.empty());
        when(alexaReminderApiHandlerMock.getDeviceTimeZone(any())).thenReturn(Calendar.getInstance().getTimeZone().getID());
        when(reminderRepositoryMock.isReadyToSync()).thenReturn(true);
        ArgumentCaptor<MyMemoReminder> changedReminder = ArgumentCaptor.forClass(MyMemoReminder.class);
        //act
        sut.syncReminders();
        //assert
        verify(alexaReminderApiHandlerMock, times(1)).createReminder(changedReminder.capture());
        assertThat(changedReminder.getValue().getTime()).isAfter(LocalTime.now(Calendar.getInstance().getTimeZone().toZoneId()));
    }

}