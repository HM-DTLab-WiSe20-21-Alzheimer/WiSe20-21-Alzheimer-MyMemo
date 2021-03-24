package edu.hm.sweng.model;

import com.amazon.ask.attributes.AttributesManager;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import edu.hm.sweng.model.dto.MyMemoDBEntry;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

@SuppressWarnings("checkstyle:DescendantToken")
public class MyMemoSharingData {

    private static final Logger LOGGER = getLogger(MyMemoSharingData.class);
    private static final String SHARING_KEY = "sharing status";
    private static final String REMINDERS_KEY = "reminders";
    private static final String PRIVATE_KEY_KEY = "private key";
    private static final int[] ASCII_RANGE = {33, 125};
    private static final int SECRET_KEY_SIZE = 25;
    private static final int[] TEMPKEY_INFO = {0, 9, 7};
    private static final String REGEX = "[^0-9]";
    private static final String REGION = "eu-central-1";
    private static final AmazonDynamoDB CLIENT = AmazonDynamoDBClientBuilder.standard()
        .withRegion(REGION)
        .build();
    private static final DynamoDBMapper MAPPER = new DynamoDBMapper(CLIENT);

    private final AttributesManager attributesManager;

    public MyMemoSharingData(AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
    }

    public static String generateTempKey(AttributesManager attributesManager) {
        //600k possible combinations
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(System.currentTimeMillis() + System.getProperties().hashCode());
        final var key = secureRandom
            .ints(TEMPKEY_INFO[0], TEMPKEY_INFO[1])
            .distinct()
            .limit(TEMPKEY_INFO[2]).toArray();
        final String keyToString = Arrays.toString(key).replaceAll(REGEX, "");
        final String privateKey = (String) attributesManager.getPersistentAttributes().get(PRIVATE_KEY_KEY);
        final List<String> value = List.of(
            LocalDate.now().toString(), LocalTime.now().toString(), privateKey
        );
        if (loadFromDB(keyToString) == null) {
            saveToDB(keyToString, value);
        } else {
            replaceWith(keyToString, value);
        }
        return keyToString;
    }

    public static List<String> loadFromDB(String key) {
        MyMemoDBEntry entry = new MyMemoDBEntry();
        entry.setKey(key);
        entry = MAPPER.load(entry);
        return entry == null ? null : entry.getValue();
    }

    public static void saveToDB(String key, List<String> value) {
        final MyMemoDBEntry entry = new MyMemoDBEntry();
        entry.setKey(key);
        entry.setValue(value);
        MAPPER.save(entry);
    }

    public static void replaceWith(String key, List<String> value) {
        MyMemoDBEntry entry = new MyMemoDBEntry();
        entry.setKey(key);
        entry = MAPPER.load(entry);
        entry.setValue(value);
        MAPPER.save(entry);
    }

    public static void erase(String privateKey) {
        var entry = new MyMemoDBEntry();
        entry.setKey(privateKey);
        entry = MAPPER.load(entry);
        MAPPER.delete(entry);
    }

    public void enableSharing() {
        final var persistentAttributes = attributesManager.getPersistentAttributes();
        final boolean isSharingEnabled = persistentAttributes.get(SHARING_KEY) != null
            && (boolean) persistentAttributes.get(SHARING_KEY);
        if (isSharingEnabled) {
            LOGGER.info("Sharing is already enabled");
        } else {
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.setSeed(System.getProperties().hashCode() + System.currentTimeMillis());
            var randomValues = secureRandom
                .ints(ASCII_RANGE[0], ASCII_RANGE[1])
                .limit(SECRET_KEY_SIZE)
                .toArray();
            StringBuilder privateKey = new StringBuilder();
            for (int value : randomValues) {
                privateKey.append((char) value);
            }
            List<String> remindersFromDb = (List<String>) persistentAttributes.get(REMINDERS_KEY);
            persistentAttributes.put(PRIVATE_KEY_KEY, privateKey.toString());
            persistentAttributes.put(SHARING_KEY, true);
            saveToDB(privateKey.toString(), remindersFromDb);
            LOGGER.info("New secret key for reminder repository generated");
            LOGGER.info("Sharing is now enabled");
            attributesManager.savePersistentAttributes();
        }
    }

    public void disableSharing() {
        final var persistentAttributes = attributesManager.getPersistentAttributes();
        final boolean isSharingEnabled = persistentAttributes.get(SHARING_KEY) != null
            && (boolean) persistentAttributes.get(SHARING_KEY);
        if (isSharingEnabled) {
            final String privateKey = (String) persistentAttributes.get(PRIVATE_KEY_KEY);
            persistentAttributes.put(PRIVATE_KEY_KEY, null);
            erase(privateKey);
            persistentAttributes.put(SHARING_KEY, false);
            LOGGER.info("Sharing is now deactivated");
        } else {
            LOGGER.info("Sharing is already disabled");
        }
        attributesManager.savePersistentAttributes();
    }
}
