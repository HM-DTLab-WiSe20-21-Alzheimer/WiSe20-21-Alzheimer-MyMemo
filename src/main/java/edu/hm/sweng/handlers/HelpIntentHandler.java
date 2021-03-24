package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;

import java.util.Optional;

public class HelpIntentHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(Predicates.intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        StringBuilder speechBuilder = new StringBuilder(1000);
        speechBuilder.append("Die folgenden Befehle beziehen sich auf eine Erinnerung mit dem Titel: Lebensmittel einkaufen. Der Titel haengt von ihren aktuellen Erinnerungen ab.")
            .append(System.lineSeparator())
            .append("Um die Erinnerung zu aendern, sage \"Aendere die Erinnerung Lebensmittel einkaufen\".")
            .append(System.lineSeparator())
            .append("Wenn die Erinnerung regelmaessig erfolgen soll, verwende \"Erinnere mich regelmaessig an Lebensmittel einkaufen\".")
            .append(System.lineSeparator())
            .append("Um alle Erinnerungen aufzulisten, sage: \"Welche Aufgaben muss ich noch machen?\".")
            .append(System.lineSeparator())
            .append("Nach dem Aufrufen der noch offenen Erinnerungen wirst du fuer abgelaufene aber noch nicht bestaetigte Aufgaben in ein paar Minuten erneut erinnert.")
            .append(System.lineSeparator())
            .append("Bestaetige deine Erinnerung mit \"Ich habe Lebensmittel einkaufen erledigt\".")
            .append(System.lineSeparator())
            .append("Um die Beschreibung aufzulisten, frage my memo mit: \"sag mir etwas zu Lebensmittel einkaufen\".")
            .append(System.lineSeparator())
            .append("Um die Erinnerung zu loeschen, sage: \"Erinnere mich nicht mehr an Lebensmittel einkaufen\".")
            .append(System.lineSeparator())
            .append("Du kannst auch eine Beschreibung aendern, indem du sagst \"ergaenze etwas zu Lebensmittel einkaufen\".");
        return input.getResponseBuilder()
            .withSpeech(speechBuilder.toString())
            .build();
    }
}
