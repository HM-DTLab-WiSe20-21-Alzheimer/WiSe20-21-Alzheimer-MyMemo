package edu.hm.sweng.model;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import edu.hm.sweng.model.dto.MyMemoReminder;
import edu.hm.sweng.model.dto.RecurrenceUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ReminderController {

    private static final String DATE_KEY = "date";
    private static final String SUBJECT_KEY = "subject";
    private static final String TIME_KEY = "time";
    private static final int DAYS_PER_WEEK = 7;
    private static final int SYNC_DELAY = 5;

    private HandlerInput handlerInput;

    private ReminderController() {
    }

    private ReminderController(HandlerInput input) {
        this.handlerInput = input;
    }

    public static ReminderController getReminderController() {
        return new ReminderController();
    }

    public ReminderController withHandlerInput(HandlerInput input) {
        return new ReminderController(input);
    }

    public ReminderRepository getRepository() {
        return new ReminderRepository(handlerInput.getAttributesManager());
    }

    public AlexaReminderApiHandler getReminderApiHandler() {
        return new AlexaReminderApiHandler(handlerInput);
    }

    public MyMemoReminder saveReminder(Map<String, Object> taskData, Optional<String> description) {
        MyMemoReminder myMemoReminder = new MyMemoReminder();
        myMemoReminder.setDate(LocalDate.parse((String) taskData.get(DATE_KEY)));
        myMemoReminder.setSubject(((String) taskData.get(SUBJECT_KEY)).replace("mit my memo", "").trim());
        myMemoReminder.setTime(LocalTime.parse((String) taskData.get(TIME_KEY)));
        if (taskData.containsKey("recurring")) {
            int recurringAmount = (Integer) taskData.get("recurringAmount");
            myMemoReminder.setRecurring(taskData.containsKey("recurring"));
            if (recurringAmount >= DAYS_PER_WEEK) {
                myMemoReminder.setRecurrenceAmount(recurringAmount / DAYS_PER_WEEK);
                myMemoReminder.setRecurrenceUnit(RecurrenceUnit.WEEK);
            } else {
                myMemoReminder.setRecurrenceAmount(recurringAmount);
                myMemoReminder.setRecurrenceUnit(RecurrenceUnit.DAY);
            }
        }
        description.ifPresent(myMemoReminder::setDescription);
        myMemoReminder.setReminderId(getReminderApiHandler().createReminder(myMemoReminder));
        getRepository().saveReminder(myMemoReminder);

        return myMemoReminder;
    }


    public void syncReminders() {
        if (getReminderApiHandler().checkPermission().isEmpty() && getRepository().isReadyToSync()) {
            ReminderRepository repository = getRepository();
            AlexaReminderApiHandler apiHandler = getReminderApiHandler();
            ZoneId zoneId = TimeZone.getTimeZone(apiHandler.getDeviceTimeZone(handlerInput.getRequestEnvelope())).toZoneId();
            final List<MyMemoReminder> reminders = repository.getRemindersFromDb().stream()
                .filter(reminder -> !reminder.isRecurring())
                .filter(reminder -> LocalDateTime.of(reminder.getDate(), reminder.getTime()).isBefore(LocalDateTime.now(zoneId)))
                .collect(Collectors.toList());
            reminders.forEach(reminder -> {
                reminder.setDate(LocalDate.now(zoneId));
                reminder.setTime(LocalTime.now(zoneId).plusMinutes(SYNC_DELAY).truncatedTo(ChronoUnit.MINUTES));
                reminder.setSubject("Hast du bereits " + reminder.getSubject() + " erledigt? Bitte bestaetige danach die Erinnerung.");
            });
            reminders.forEach(apiHandler::createReminder);
        }
    }


    public boolean confirmReminder(String subject) {
        boolean result = false;
        Optional<MyMemoReminder> foundReminder = getRepository().getReminderBySubject(subject);
        if (foundReminder.isPresent()) {
            deleteReminder(subject);
            result = true;
        }
        return result;
    }

    public void deleteReminder(String subject) {
        List<MyMemoReminder> deletedReminders = getRepository().deleteReminder(subject);

        AlexaReminderApiHandler reminderApiHandler = getReminderApiHandler();
        deletedReminders.forEach(reminder -> reminderApiHandler.deleteReminder(reminder.getReminderId()));
    }

    public boolean updateWithData(Map<String, Object> taskData) {
        // data used to identify the reminder to update
        final String subject = Objects.requireNonNull((String) taskData.get(SUBJECT_KEY));
        final String date = (String) taskData.get(DATE_KEY);
        final String time = (String) taskData.get(TIME_KEY);

        // data used to update
        final String newDate = (String) taskData.get("newDate");
        final String newTime = (String) taskData.get("newTime");
        final String newDescription = (String) taskData.get("newDescription");

        List<MyMemoReminder> remindersFromDb = getRepository()
            .getRemindersFromDb()
            .parallelStream()
            .filter(reminder -> matches(subject, date, time, reminder))
            .collect(Collectors.toList());

        if (remindersFromDb.size() == 1) {
            List<MyMemoReminder> updatedReminders = getRepository().updateReminder(
                remindersFromDb.get(0),
                Optional.ofNullable(newDate),
                Optional.ofNullable(newTime),
                Optional.ofNullable(newDescription));

            AlexaReminderApiHandler reminderApiHandler = getReminderApiHandler();
            updatedReminders.forEach(reminderApiHandler::updateReminder);
            return true;
        } else {
            return false;
        }

    }

    public boolean noReminders() {
        return getRepository().getRemindersFromDb().isEmpty();
    }

    private boolean matches(String subject, String date, String time, MyMemoReminder reminder) {
        boolean returnVal = reminder.getSubject().equals(subject);
        if (returnVal) {
            if (date != null) {
                returnVal = reminder.getDate().equals(LocalDate.parse(date));
            }
            if (time != null) {
                returnVal = reminder.getTime().equals(LocalTime.parse(time));
            }
        }
        return returnVal;
    }


}
