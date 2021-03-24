package edu.hm.sweng.model;

import com.amazon.ask.attributes.AttributesManager;
import edu.hm.sweng.model.dto.MyMemoReminder;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christof Huber
 * @version 26.12.20
 */
public class ReminderRepositoryTest {

    private AttributesManager attributesManager;
    private ReminderRepository sut;

    @Before
    public void setUp() {
        attributesManager = mock(AttributesManager.class);
        Map<String, Object> persistentAttributes = new HashMap<>();
        when(attributesManager.getPersistentAttributes()).thenReturn(persistentAttributes);

        sut = new ReminderRepository(attributesManager);
    }

    @Test
    public void test_saveReminder_withDescription() {
        // arrange
        MyMemoReminder reminder = new MyMemoReminder();

        // act
        sut.saveReminder(reminder);

        // assert
        verify(attributesManager, times(3)).getPersistentAttributes();
        assertThat(attributesManager.getPersistentAttributes()).hasSize(1)
            .containsKey("reminders");
        verify(attributesManager).savePersistentAttributes();
    }

    @Test
    public void test_saveReminder_containsOldReminder() {
        // arrange
        MyMemoReminder reminder = new MyMemoReminder();
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"subject\":\"brot\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);

        // act
        sut.saveReminder(reminder);

        // assert
        verify(attributesManager, times(3)).getPersistentAttributes();
        assertThat(attributesManager.getPersistentAttributes()).hasSize(1)
            .containsKey("reminders");
        assertThat(attributesManager.getPersistentAttributes().get("reminders")).asList()
            .hasSize(2)
            .first()
            .asString()
            .contains("brot");
        verify(attributesManager).savePersistentAttributes();
    }


    @Test
    public void test_deleteReminderFound() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);

        // act
        List<MyMemoReminder> deletedReminders = sut.deleteReminder("GNAMPF");

        // assert
        verify(attributesManager, times(4)).getPersistentAttributes();
        assertThat(attributesManager.getPersistentAttributes()).hasSize(1)
            .containsKey("reminders");
        assertThat(attributesManager.getPersistentAttributes().get("reminders")).asList()
            .isEmpty();
        verify(attributesManager).savePersistentAttributes();

        assertThat(deletedReminders).hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("subject", "GNAMPF")
            .hasFieldOrPropertyWithValue("date", LocalDate.parse("2012-12-21"))
            .hasFieldOrPropertyWithValue("time", LocalTime.parse("12:00"))
            .hasFieldOrPropertyWithValue("description", null);
    }

    @Test
    public void test_deleteReminderNotFound() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);

        // act
        List<MyMemoReminder> deletedReminders = sut.deleteReminder("other reminder");

        // assert
        verify(attributesManager, times(4)).getPersistentAttributes();
        assertThat(attributesManager.getPersistentAttributes()).hasSize(1)
            .containsKey("reminders");
        assertThat(attributesManager.getPersistentAttributes().get("reminders")).asList()
            .isNotEmpty();
        verify(attributesManager).savePersistentAttributes();

        assertThat(deletedReminders).isEmpty();
    }

    @Test
    public void test_updateReminder() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);

        MyMemoReminder reminder = new MyMemoReminder(
            LocalDate.parse("2012-12-21"),
            LocalTime.parse("12:00"),
            "GNAMPF",
            null,
            null);

        //act
        sut.updateReminder(reminder, Optional.of("2015-11-11"), Optional.of("10:35"), Optional.empty());

        //assert
        assertThat(testAttributes).containsValue(List.of("{\"date\":{\"year\":2015,\"month\":11,\"day\":11},\"recurring\":false,\"recurrenceAmount\":0,\"time\":{\"hour\":10,\"minute\":35,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}"));
    }

    @Test
    public void test_updateReminderNotFound() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);

        MyMemoReminder reminder = new MyMemoReminder(
            LocalDate.parse("2049-01-02"),
            LocalTime.parse("01:22"),
            "GNAMPF",
            null,
            null);

        //act
        sut.updateReminder(reminder, Optional.of("2015-11-11"), Optional.of("10:35"), Optional.empty());

        //assert
        assertThat(testAttributes).containsValue(List.of("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"recurring\":false,\"recurrenceAmount\":0,\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\"}"));
    }

    @Test
    public void test_updateReminderDescription() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\",\"description\":\"ABCDEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);

        MyMemoReminder reminder = new MyMemoReminder(
            LocalDate.parse("2012-12-21"),
            LocalTime.parse("12:00"),
            "GNAMPF",
            "ABCDEFG",
            null);

        //act
        sut.updateReminder(reminder, Optional.empty(), Optional.empty(), Optional.of("ZZZZ"));

        //assert
        assertThat(testAttributes).containsValue(List.of("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"recurring\":false,\"recurrenceAmount\":0,\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\",\"description\":\"ZZZZ\"}"));
    }


    //Equals Tests

    @Test
    public void test_equalsSameValues() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"GNAMPF\",\"description\":\"ABCDEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2012-12-21"),
                                                     LocalTime.parse("12:00"), "GNAMPF", "ABCDEFG", null);
        MyMemoReminder reminder2 = new MyMemoReminder(LocalDate.parse("2012-12-21"),
                                                      LocalTime.parse("12:00"), "GNAMPF", "ABCDEFG", null);

        //act
        boolean have = reminder.equals(reminder2);

        //assert
        assertThat(have).isTrue();
    }

    @Test
    public void test_equalsSameSubjectDifferentTime() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":11,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCDEFG\"}");
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCDEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2012-11-21"),
                                                     LocalTime.parse("12:00"), "FIRSTSUB", "ABCDEFG", null);
        MyMemoReminder reminder2 = new MyMemoReminder(LocalDate.parse("2012-12-21"),
                                                      LocalTime.parse("12:00"), "FIRSTSUB", "ABCDEFG", null);

        //act
        boolean have = reminder.equals(reminder2);

        //assert
        assertThat(have).isFalse();
    }


    @Test
    public void test_equalsSameIdDifferentValues() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2011,\"month\":11,\"day\":11},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCEFG\"}");
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"SECONDSUB\",\"description\":\"ABCDEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2011-11-11"),
                                                     LocalTime.parse("12:00"), "FIRSTSUB", "ABCEFG", "1");
        MyMemoReminder reminder2 = new MyMemoReminder(LocalDate.parse("2012-12-21"),
                                                      LocalTime.parse("12:00"), "SECONDSUB", "ABCDEFG", "1");

        //act
        boolean have = reminder.equals(reminder2);

        //assert
        assertThat(have).isFalse();
    }

    @Test
    public void test_equalsSameReminder() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2011,\"month\":11,\"day\":11},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2011-11-11"), LocalTime.parse("12:00"),
                                                     "FIRSTSUB", "ABCEFG", "1");

        //act
        boolean have = reminder.equals(reminder);

        //assert
        assertThat(have).isTrue();
    }

    @Test
    public void test_equalsDifferentInstance() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2011,\"month\":11,\"day\":11},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2011-11-11"),
                                                     LocalTime.parse("12:00"), "FIRSTSUB", "ABCEFG", "1");

        //act
        boolean have = reminder.equals("FAIL");

        //assert
        assertThat(have).isFalse();
    }

    @Test
    public void test_hashcode() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2011,\"month\":11,\"day\":11},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCEFG\"}");
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"SECONDSUB\",\"description\":\"ABCDEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2011-11-11"),
                                                     LocalTime.parse("12:00"), "FIRSTSUB", "ABCEFG", "1");
        MyMemoReminder reminder2 = new MyMemoReminder(LocalDate.parse("2012-12-21"),
                                                      LocalTime.parse("12:00"), "SECONDSUB", "ABCDEFG", "1");

        //act
        int haveFirst = reminder.hashCode();
        int haveSecond = reminder2.hashCode();

        //assert
        assertThat(haveFirst).isNotEqualTo(haveSecond);
    }

    @Test
    public void test_setSubject() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2011,\"month\":11,\"day\":11},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2011-11-11"),
                                                     LocalTime.parse("12:00"), "FIRSTSUB", "ABCEFG", "1");

        //act
        reminder.setSubject("TEST");
        String have = reminder.getSubject();
        String want = "TEST";

        //assert
        assertThat(have).isEqualTo(want);
    }

    @Test
    public void test_setReminderId() {
        // arrange
        Map<String, Object> testAttributes = new HashMap<>();
        List<String> testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2011,\"month\":11,\"day\":11},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"FIRSTSUB\",\"description\":\"ABCEFG\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        MyMemoReminder reminder = new MyMemoReminder(LocalDate.parse("2011-11-11"),
                                                     LocalTime.parse("12:00"), "FIRSTSUB", "ABCEFG", "1");

        //act
        reminder.setReminderId("2");
        String have = reminder.getReminderId();
        String want = "2";

        //assert
        assertThat(have).isEqualTo(want);
    }

    @Test
    public void test_getReminderSubjectsForTimeframe_firstDate() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        final var testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Timon\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Pumba\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2013,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Lilo\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2013,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Stitch\",\"description\":\"Akuna matata\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final var result = sut.getReminderSubjectsForTimeframe("2012-12-21", null, "heute");
        //assert
        assertThat(result).contains("Timon")
            .contains("Pumba")
            .hasSize(2);
    }

    @Test
    public void test_getReminderSubjectsForTimeframe_betweenTwoDates() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        final var testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Timon\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2013,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Pumba\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2013,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Lilo\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2054,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Stitch\",\"description\":\"Akuna matata\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final var result = sut.getReminderSubjectsForTimeframe("2013-01-01", "2014-01-01", null);
        //assert
        assertThat(result).contains("Lilo")
            .contains("Pumba")
            .hasSize(2);
    }

    @Test
    public void test_getReminderSubjectsForTimeframe_timeframe_today() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        final var testList = new ArrayList<>();
        final var today = LocalDate.now();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Timon\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":" + today.getYear() + ",\"month\":" + today.getMonthValue() + ",\"day\":" + today.getDayOfMonth() + "},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Pumba\",\"description\":\"Akuna matata\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final var result = sut.getReminderSubjectsForTimeframe(null, null, "heute");
        //assert
        assertThat(result).contains("Pumba")
            .hasSize(1);
    }

    @Test
    public void test_getReminderSubjectsForTimeframe_timeframe_tomorrow() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        final var testList = new ArrayList<>();
        final var date = LocalDate.now().plusDays(1);
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Timon\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":" + date.getYear() + ",\"month\":" + date.getMonthValue() + ",\"day\":" + date.getDayOfMonth() + "},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Pumba\",\"description\":\"Akuna matata\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final var result = sut.getReminderSubjectsForTimeframe(null, null, "morgen");
        //assert
        assertThat(result).contains("Pumba")
            .hasSize(1);
    }

    @Test
    public void test_getReminderSubjectsForTimeframe_timeframe_nextWeek() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        final var testList = new ArrayList<>();
        final var date = LocalDate.now().plusDays(7);
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Timon\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2199,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Simba\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":" + date.getYear() + ",\"month\":" + date.getMonthValue() + ",\"day\":" + date.getDayOfMonth() + "},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Pumba\",\"description\":\"Akuna matata\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final var result = sut.getReminderSubjectsForTimeframe(null, null, "woche");
        //assert
        assertThat(result).contains("Pumba")
            .hasSize(1);
    }

    @Test
    public void test_getReminderSubjectsForTimeframe_getAll() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        final var testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Timon\",\"description\":\"Akuna matata\"}");
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Pumba\",\"description\":\"Akuna matata\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final var result = sut.getReminderSubjectsForTimeframe(null, null, null);
        //assert
        assertThat(result).contains("Pumba")
            .contains("Timon")
            .hasSize(2);
    }

    @Test
    public void test_getReminderBySubject() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        final var testList = new ArrayList<>();
        testList.add("{\"date\":{\"year\":2012,\"month\":12,\"day\":21},\"time\":{\"hour\":12,\"minute\":0,\"second\":0,\"nano\":0},\"subject\":\"Simba\",\"description\":\"Akuna matata\"}");
        testAttributes.put("reminders", testList);
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final var result = sut.getReminderBySubject("Simba");
        //arrange
        assertThat(result).isPresent();
        assertThat(result.get().getSubject()).isEqualTo("Simba");
        assertThat(result.get().getDescription()).isEqualTo("Akuna matata");
    }

    @Test
    public void test_isReadyToSync_new() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final boolean result = sut.isReadyToSync();
        //assert
        assertThat(result).isTrue();
        assertThat(testAttributes).containsKey("syncTimestamp");
    }

    @Test
    public void test_isReadyToSync_true() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        testAttributes.put("syncTimestamp", LocalDateTime.now().minusMinutes(6).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final boolean result = sut.isReadyToSync();
        //assert
        assertThat(result).isTrue();
    }

    @Test
    public void test_isReadyToSync_false() {
        //arrange
        final var testAttributes = new HashMap<String, Object>();
        testAttributes.put("syncTimestamp", LocalDateTime.now().minusMinutes(4).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(attributesManager.getPersistentAttributes()).thenReturn(testAttributes);
        //act
        final boolean result = sut.isReadyToSync();
        //assert
        assertThat(result).isFalse();
    }

}