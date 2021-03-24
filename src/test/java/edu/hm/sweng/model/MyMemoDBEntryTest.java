package edu.hm.sweng.model;

import edu.hm.sweng.model.dto.MyMemoDBEntry;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MyMemoDBEntryTest {

    @Test
    public void coverageTest() {
        final var entry = new MyMemoDBEntry();
        entry.setKey("1");
        entry.setValue(List.of("2"));

        assertThat(entry.getKey()).isEqualTo("1");
        assertThat(entry.getValue()).isEqualTo(List.of("2"));
    }

}
