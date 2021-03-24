package edu.hm.sweng.model;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Permissions;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.services.reminderManagement.AlertInfo;
import com.amazon.ask.model.services.reminderManagement.AlertInfoSpokenInfo;
import com.amazon.ask.model.services.reminderManagement.PushNotification;
import com.amazon.ask.model.services.reminderManagement.PushNotificationStatus;
import com.amazon.ask.model.services.reminderManagement.Recurrence;
import com.amazon.ask.model.services.reminderManagement.RecurrenceDay;
import com.amazon.ask.model.services.reminderManagement.ReminderManagementServiceClient;
import com.amazon.ask.model.services.reminderManagement.ReminderRequest;
import com.amazon.ask.model.services.reminderManagement.SpokenText;
import com.amazon.ask.model.services.reminderManagement.Trigger;
import com.amazon.ask.model.services.reminderManagement.TriggerType;
import com.amazon.ask.model.services.ups.UpsServiceClient;
import com.amazon.ask.response.ResponseBuilder;
import edu.hm.sweng.model.dto.MyMemoReminder;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AlexaReminderApiHandler {

    private static final Map<DayOfWeek, RecurrenceDay> RECURRENCE_DAY_MAP = Map.of(DayOfWeek.MONDAY, RecurrenceDay.MO,
                                                                                   DayOfWeek.TUESDAY, RecurrenceDay.TU,
                                                                                   DayOfWeek.WEDNESDAY, RecurrenceDay.WE,
                                                                                   DayOfWeek.THURSDAY, RecurrenceDay.TH,
                                                                                   DayOfWeek.FRIDAY, RecurrenceDay.FR,
                                                                                   DayOfWeek.SATURDAY, RecurrenceDay.SA,
                                                                                   DayOfWeek.SUNDAY, RecurrenceDay.SU);

    private final ReminderManagementServiceClient reminderManagementService;
    private final UpsServiceClient upsService;
    private final RequestEnvelope requestEnvelope;

    public AlexaReminderApiHandler(HandlerInput input) {
        this.reminderManagementService = input.getServiceClientFactory().getReminderManagementService();
        this.upsService = input.getServiceClientFactory().getUpsService();
        this.requestEnvelope = input.getRequestEnvelope();
    }

    public Optional<Response> checkPermission() {
        Permissions permissions = requestEnvelope.getContext().getSystem().getUser().getPermissions();
        if (null == permissions || null == permissions.getConsentToken()) {
            String speechText = "Um Erinnerungen zu erzeugen musst du Alexa die Erlaubnis geben. Dazu kannst du die Karte nutzen, die ich der Alexa App geschickt habe.";
            List<String> list = new ArrayList<>();
            list.add("alexa::alerts:reminders:skill:readwrite");
            return new ResponseBuilder()
                .withSpeech(speechText)
                .withAskForPermissionsConsentCard(list)
                .build();
        } else {
            return Optional.empty();
        }
    }

    public String createReminder(MyMemoReminder reminder) {
        return reminderManagementService.createReminder(getReminderRequest(reminder)).getAlertToken();
    }

    public void deleteReminder(String reminderId) {
        reminderManagementService.deleteReminder(reminderId);
    }

    public void updateReminder(MyMemoReminder reminder) {
        reminderManagementService.updateReminder(reminder.getReminderId(), getReminderRequest(reminder));
    }

    public String getDeviceTimeZone(RequestEnvelope envelope) {
        return upsService.getSystemTimeZone(envelope.getContext().getSystem().getDevice().getDeviceId());
    }

    private ReminderRequest getReminderRequest(MyMemoReminder reminder) {
        AlertInfo alertInfo = AlertInfo.builder()
            .withSpokenInfo(AlertInfoSpokenInfo.builder()
                                .addContentItem(SpokenText.builder()
                                                    .withText(reminder.getSubject())
                                                    .withLocale(requestEnvelope.getRequest().getLocale())
                                                    .build())
                                .build())
            .build();

        Trigger.Builder trigger = Trigger.builder()
            .withType(TriggerType.SCHEDULED_ABSOLUTE)
            .withScheduledTime(LocalDateTime.of(reminder.getDate(), reminder.getTime()))
            .withTimeZoneId(getDeviceTimeZone(requestEnvelope));
        if (reminder.isRecurring()) {
            trigger.withRecurrence(
                Recurrence.builder()
                    .withFreq(reminder.getRecurrenceUnit().getKey())
                    .withInterval(reminder.getRecurrenceAmount())
                    .addByDayItem(RECURRENCE_DAY_MAP.get(reminder.getDate().getDayOfWeek()))
                    .build()
            );
        }
        return ReminderRequest.builder()
            .withAlertInfo(alertInfo)
            .withRequestTime(OffsetDateTime.now())
            .withTrigger(trigger.build())
            .withPushNotification(PushNotification.builder()
                                      .withStatus(PushNotificationStatus.ENABLED)
                                      .build())
            .build();
    }
}
