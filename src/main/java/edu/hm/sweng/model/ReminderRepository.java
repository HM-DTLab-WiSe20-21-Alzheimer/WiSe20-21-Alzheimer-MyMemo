package edu.hm.sweng.model;

import com.amazon.ask.attributes.AttributesManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.hm.sweng.model.dto.MyMemoReminder;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.LogManager.getLogger;

public class ReminderRepository {

    public static final String REMINDERS_KEY = "reminders";
    public static final Gson GSON_BUILDER = new GsonBuilder().create();
    private static final Logger LOGGER = getLogger(ReminderRepository.class);
    private static final String SYNC_KEY = "syncTimestamp";
    private static final int SYNC_DELAY = 5;
    private static final String PRIVATE_KEY_KEY = "private key";
    private static final String SHARING_KEY = "sharing status";

    private final AttributesManager attributesManager;

    public ReminderRepository(AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
    }

    public void saveReminder(MyMemoReminder myMemoReminder) {
        List<MyMemoReminder> remindersFromDb = getRemindersFromDb();
        remindersFromDb.add(myMemoReminder);
        saveRemindersToDb(remindersFromDb);
    }

    public List<String> getReminderSubjectsForTimeframe(final String firstDate, final String secondDate, final String timeframe) {
        Predicate<MyMemoReminder> reminderFilter = reminder -> true;
        if (firstDate != null) {
            reminderFilter = filterBetweenTwoDates(firstDate, secondDate);
        } else if (timeframe != null) {
            reminderFilter = filterTimeframe(timeframe);
        }

        return getRemindersFromDb().parallelStream()
            .filter(reminderFilter)
            .map(MyMemoReminder::getSubject)
            .collect(Collectors.toList());
    }

    /**
     * Looks for the Reminder in the Database
     *
     * @param subject the subject of the reminder
     * @return optional of the Reminder
     */
    public Optional<MyMemoReminder> getReminderBySubject(String subject) {
        return getRemindersFromDb().stream()
            .filter(reminder -> reminder.getSubject().equals(subject))
            .findFirst();
    }

    public List<MyMemoReminder> updateReminder(MyMemoReminder reminder, Optional<String> newDate, Optional<String> newTime, Optional<String> newDescription) {
        List<MyMemoReminder> remindersToUpdate = getRemindersFromDb().parallelStream()
            .map(rem -> {
                if (rem.equals(reminder)) {
                    newDate.ifPresent(date -> rem.setDate(LocalDate.parse(date)));
                    newTime.ifPresent(time -> rem.setTime(LocalTime.parse(time)));
                    newDescription.ifPresent(rem::setDescription);
                }
                return rem;
            }).collect(Collectors.toList());
        List<MyMemoReminder> updatedReminders = remindersToUpdate.stream()
            .filter(getRemindersFromDb()::contains)
            .collect(Collectors.toList());
        saveRemindersToDb(remindersToUpdate);

        return updatedReminders;
    }

    public List<MyMemoReminder> deleteReminder(final String subject) {
        LOGGER.info("received subject to delete: {}", subject);

        List<MyMemoReminder> remindersFromDb = getRemindersFromDb().parallelStream()
            .filter(reminder -> !reminder.getSubject().equals(subject)) //filters out all matching subjects
            .collect(Collectors.toList());
        List<MyMemoReminder> deletedReminders = getRemindersFromDb().stream()
            .dropWhile(remindersFromDb::contains)
            .collect(Collectors.toList());
        saveRemindersToDb(remindersFromDb);
        return deletedReminders;
    }

    public List<MyMemoReminder> getRemindersFromDb() {
        final Map<String, Object> persistentAttributes = attributesManager.getPersistentAttributes();
        List<String> remindersFromDb = (List<String>) persistentAttributes.get(REMINDERS_KEY);

        // this happens for the first reminder of the user
        if (remindersFromDb == null) {
            remindersFromDb = new ArrayList<>();
        }

        return remindersFromDb.parallelStream()
            .map(reminder -> GSON_BUILDER.fromJson(reminder, MyMemoReminder.class))
            .collect(Collectors.toList());
    }

    public boolean isReadyToSync() {
        final boolean returnVal;
        if (attributesManager.getPersistentAttributes().containsKey(SYNC_KEY)) {
            LocalDateTime timestamp = LocalDateTime.parse((String) attributesManager.getPersistentAttributes().get(SYNC_KEY),
                                                          DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            returnVal = timestamp.plusMinutes(SYNC_DELAY).isBefore(LocalDateTime.now());
        } else {
            returnVal = true;
        }
        if (returnVal) {
            attributesManager.getPersistentAttributes().put(SYNC_KEY, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            attributesManager.savePersistentAttributes();
        }
        return returnVal;
    }

    private void saveRemindersToDb(List<MyMemoReminder> reminders) {
        List<String> remindersForDb = reminders.parallelStream()
            .map(GSON_BUILDER::toJson)
            .collect(Collectors.toList());

        attributesManager.getPersistentAttributes().put(REMINDERS_KEY, remindersForDb);
        attributesManager.savePersistentAttributes();

        final boolean isSharingEnabled = attributesManager.getPersistentAttributes().get(SHARING_KEY) != null
            && (boolean) attributesManager.getPersistentAttributes().get(SHARING_KEY);
        if (isSharingEnabled) {
            final String key = (String) attributesManager.getPersistentAttributes().get(PRIVATE_KEY_KEY);
            LOGGER.info(key);
            MyMemoSharingData.replaceWith(key, remindersForDb);
        }
    }

    private Predicate<MyMemoReminder> filterBetweenTwoDates(String firstDate, String secondDate) {
        final Predicate<MyMemoReminder> firstFilter;
        //if we get secondDate, we know we got two dates from the user.
        if (secondDate == null) {
            firstFilter = reminder -> LocalDate.parse(firstDate).isEqual(reminder.getDate());
        }
        //else we have the first, because the slots are not empty
        else {
            firstFilter = reminder -> LocalDate.parse(firstDate).isBefore(reminder.getDate())
                && LocalDate.parse(secondDate).isAfter(reminder.getDate());
        }
        return firstFilter;
    }

    private Predicate<MyMemoReminder> filterTimeframe(String timeframe) {
        final int week = 7;
        final LocalDate today = LocalDate.now();
        final Map<String, Predicate<MyMemoReminder>> filters = Map.of(
            //From sunday to monday, because both days are not included
            "woche", reminder -> reminder.getDate().isAfter(today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)))
                && reminder.getDate().isBefore(today.plusDays(week).with(TemporalAdjusters.next(DayOfWeek.MONDAY))),
            "morgen", reminder -> reminder.getDate().isEqual(today.plusDays(1)),
            "heute", reminder -> reminder.getDate().isEqual(today)
        );
        return filters.get(timeframe);
    }
}

