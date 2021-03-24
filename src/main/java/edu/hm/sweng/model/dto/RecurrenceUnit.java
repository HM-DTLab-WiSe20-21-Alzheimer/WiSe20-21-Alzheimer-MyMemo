package edu.hm.sweng.model.dto;

import com.amazon.ask.model.services.reminderManagement.RecurrenceFreq;

public enum RecurrenceUnit {

    WEEK(RecurrenceFreq.WEEKLY), DAY(RecurrenceFreq.DAILY);
    private final RecurrenceFreq key;


    RecurrenceUnit(RecurrenceFreq key) {
        this.key = key;
    }

    public RecurrenceFreq getKey() {
        return key;
    }

}
