package edu.hm.sweng.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class MyMemoReminder {

    private LocalDate date;

    private boolean recurring;

    private int recurrenceAmount;

    private RecurrenceUnit recurrenceUnit;

    private LocalTime time;

    private String subject;

    private String description;

    private String reminderId;

    public MyMemoReminder() {
    }

    public MyMemoReminder(LocalDate date, LocalTime time, String subject, String description, String reminderId) {
        this.date = date;
        this.time = time;
        this.subject = subject;
        this.description = description;
        this.reminderId = reminderId;
        this.recurring = false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MyMemoReminder)) {
            return false;
        }
        MyMemoReminder that = (MyMemoReminder) other;
        return Objects.equals(getDate(), that.getDate()) &&
            Objects.equals(getTime(), that.getTime()) &&
            Objects.equals(getSubject(), that.getSubject()) &&
            Objects.equals(getDescription(), that.getDescription()) &&
            Objects.equals(getReminderId(), that.getReminderId()) &&
            Objects.equals(getRecurrenceUnit(), that.getRecurrenceUnit()) &&
            Objects.equals(getRecurrenceAmount(), that.getRecurrenceAmount()) &&
            Objects.equals(isRecurring(), that.isRecurring());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getTime(), getSubject(), getDescription(), getReminderId(), getRecurrenceAmount(), getRecurrenceUnit(), isRecurring());
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public int getRecurrenceAmount() {
        return recurrenceAmount;
    }

    public void setRecurrenceAmount(int recurrenceAmount) {
        this.recurrenceAmount = recurrenceAmount;
    }

    public RecurrenceUnit getRecurrenceUnit() {
        return recurrenceUnit;
    }

    public void setRecurrenceUnit(RecurrenceUnit recurrenceUnit) {
        this.recurrenceUnit = recurrenceUnit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    @Override
    public String toString() {
        return "MyMemoReminder{" +
            "date=" + date +
            ", time=" + time +
            ", subject='" + subject + '\'' +
            ", description='" + description + '\'' +
            ", reminderId='" + reminderId + '\'' +
            ", recurring='" + recurring + '\'' +
            ", recurrenceUnit='" + recurrenceUnit + '\'' +
            ", recurrenceAmount='" + recurrenceAmount + '\'' +
            '}';
    }
}
