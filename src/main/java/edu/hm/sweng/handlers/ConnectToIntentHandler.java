package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.MyMemoSharingData;
import edu.hm.sweng.model.TrustedReminderRepositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectToIntentHandler implements IntentRequestHandler {

    private static final int FIVE_MIN = 5;

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("ConnectToIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        final String name = intentRequest.getIntent().getSlots().get("name").getValue();
        String key = intentRequest.getIntent().getSlots().get("key").getValue();
        final var trustedRepos = new TrustedReminderRepositories(input.getAttributesManager());

        var keyDigits = List.of(key.split("\\s"));
        keyDigits = keyDigits.stream().map(this::convertToNumVal).collect(Collectors.toList());
        key = String.join("", keyDigits);
        final var dataFromDB = MyMemoSharingData.loadFromDB(key);
        final String response;
        if (checkValidity(dataFromDB)) {
            final String privateKey = dataFromDB.get(2);
            trustedRepos.add(name, privateKey);
            response = String.format("Du hast nun zugriff auf %s My Memo reminder", name);
        } else {
            response = "Dein Code ist ungueltig. Bitte versuche es erneut.";
        }
        return input.getResponseBuilder()
            .withSpeech(response)
            .withShouldEndSession(true)
            .build();
    }

    private boolean checkValidity(List<String> dataFromDB) {
        boolean isValid = false;
        if (dataFromDB != null) {
            var date = LocalDate.parse(dataFromDB.get(0));
            var time = LocalTime.parse(dataFromDB.get(1));
            isValid = date.isEqual(LocalDate.now())
                && time.plusMinutes(FIVE_MIN).isAfter(LocalTime.now());
        }
        return isValid;
    }

    private String convertToNumVal(String number) {
        final var numberVals =
            Map.of("null", "0",
                   "eins", "1",
                   "zwei", "2",
                   "drei", "3",
                   "vier", "4",
                   "fünf", "5",
                   "sechs", "6",
                   "sieben", "7",
                   "acht", "8",
                   "neun", "9"
            );
        return numberVals.get(number);
    }
}
