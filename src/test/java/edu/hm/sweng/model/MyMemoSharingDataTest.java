package edu.hm.sweng.model;

import com.amazon.ask.attributes.AttributesManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MyMemoSharingDataTest {

    private MockedStatic<MyMemoSharingData> settings;
    private AttributesManager attributesManager;
    private Map<String, Object> persistenceAttributes;
    private MyMemoSharingData spy;

    @Before
    public void setUp() {
        attributesManager = Mockito.mock(AttributesManager.class);
        persistenceAttributes = new HashMap<>();
        when(attributesManager.getPersistentAttributes()).thenReturn(persistenceAttributes);
        settings = mockStatic(MyMemoSharingData.class);
        spy = spy(new MyMemoSharingData(attributesManager));
    }

    @After
    public void close() {
        settings.close();
    }

    @Test
    public void test_enableSharing_whileDisabled1() {
        // arrange
        persistenceAttributes.put("sharing status", null);
        persistenceAttributes.put("private key", null);

        // act
        spy.enableSharing();
        final var sharingStatus = (boolean) persistenceAttributes.get("sharing status");
        final var privateKey = (String) persistenceAttributes.get("private key");

        // assert
        assertThat(sharingStatus).isTrue();
        assertThat(privateKey).hasSize(25);
    }

    @Test
    public void test_enableSharing_whileDisabled2() {
        // arrange
        persistenceAttributes.put("sharing status", false);
        persistenceAttributes.put("private key", null);

        // act
        spy.enableSharing();
        final var sharingStatus = (boolean) persistenceAttributes.get("sharing status");
        final var privateKey = (String) persistenceAttributes.get("private key");

        // assert
        assertThat(sharingStatus).isTrue();
        assertThat(privateKey).hasSize(25);
    }

    @Test
    public void test_enableSharing_whileEnabled() {
        // arrange
        persistenceAttributes.put("sharing status", true);
        persistenceAttributes.put("private key", "simba");

        // act
        spy.enableSharing();
        final var sharingStatus = (boolean) persistenceAttributes.get("sharing status");
        final var privateKey = (String) persistenceAttributes.get("private key");

        // assert
        assertThat(sharingStatus).isTrue();
        assertThat(privateKey).isEqualTo("simba");
    }

    @Test
    public void test_disableSharing_whileDisabled1() {
        // arrange
        persistenceAttributes.put("sharing status", false);
        persistenceAttributes.put("private key", "simba");

        // act
        spy.disableSharing();
        final var sharingStatus = (boolean) persistenceAttributes.get("sharing status");
        final var privateKey = (String) persistenceAttributes.get("private key");

        // assert
        assertThat(sharingStatus).isFalse();
        //should not modify key
        assertThat(privateKey).isEqualTo("simba");
    }

    @Test
    public void test_disableSharing_whileDisabled2() {
        // arrange
        persistenceAttributes.put("sharing status", null);
        persistenceAttributes.put("private key", "empty");

        // act
        spy.disableSharing();
        final var sharingStatus = persistenceAttributes.get("sharing status");
        final var privateKey = persistenceAttributes.get("private key");

        // assert
        assertThat(sharingStatus).isNull();
        //should not modify key
        assertThat(privateKey).isEqualTo("empty");
    }

    @Test
    public void test_disableSharing_whileEnabled() {
        // arrange
        persistenceAttributes.put("sharing status", true);
        persistenceAttributes.put("private key", "simba");

        // act
        spy.disableSharing();
        final var sharingStatus = (boolean) persistenceAttributes.get("sharing status");
        final var privateKey = (String) persistenceAttributes.get("private key");

        // assert
        assertThat(sharingStatus).isFalse();
        assertThat(privateKey).isNull();
    }

    @Test
    public void test_generateTempKey() {
        // arrange
        persistenceAttributes.put("private key", "simba");
        when(MyMemoSharingData.generateTempKey(any())).thenCallRealMethod();

        // act
        final String key = MyMemoSharingData.generateTempKey(attributesManager);

        // assert
        assertThat(key).hasSize(7);
        assertThat(key).isBetween("0000000", "9999999");
    }

}
