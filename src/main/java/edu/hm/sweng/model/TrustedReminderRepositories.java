package edu.hm.sweng.model;

import com.amazon.ask.attributes.AttributesManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.hm.sweng.model.dto.MyMemoReminder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrustedReminderRepositories {

    public static final String TRUSTED_KEY = "trusted reminders";
    public static final Gson GSON_BUILDER = new GsonBuilder().create();
    private final AttributesManager attributesManager;
    private final Map<String, Object> persistentAttributes;

    public TrustedReminderRepositories(AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
        persistentAttributes = attributesManager.getPersistentAttributes();
    }

    public void add(String name, String key) {
        final var trustedReminderKeys = (Map<String, String>) persistentAttributes.get(TRUSTED_KEY);
        if (trustedReminderKeys == null) {
            persistentAttributes.put(TRUSTED_KEY, Map.of(name, key));
        } else {
            trustedReminderKeys.put(name, key);
            persistentAttributes.put(TRUSTED_KEY, trustedReminderKeys);
        }
        attributesManager.savePersistentAttributes();
    }

    public List<MyMemoReminder> get(String name) {
        final var trustedKeys = (Map<String, String>) persistentAttributes.get(TRUSTED_KEY);
        List<String> reminders = null;
        final String key = trustedKeys == null ? null : trustedKeys.get(name);
        if (key != null) {
            reminders = MyMemoSharingData.loadFromDB(key);
        }
        return reminders == null ?
            null : reminders.parallelStream()
            .map(reminder -> GSON_BUILDER.fromJson(reminder, MyMemoReminder.class))
            .collect(Collectors.toList());
    }
}
