package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.LaunchRequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.requestType;

public class CustomLaunchRequestHandler implements LaunchRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, LaunchRequest launchRequest) {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, LaunchRequest launchRequest) {
        StringBuilder speechBuilder = new StringBuilder(1000);
        speechBuilder.append("Willkommen zu my memo!")
            .append(System.lineSeparator())
            .append("Du kannst eine Erinnerung erstellen indem du: \"Erinnere mich an Titel\", sagst.")
            .append(System.lineSeparator())
            .append("Wenn du weitere Informationen oder Hilfe benoetigst, frag einfach my memo danach!");
        return input.getResponseBuilder()
            .withSpeech(speechBuilder.toString())
            .build();
    }
}