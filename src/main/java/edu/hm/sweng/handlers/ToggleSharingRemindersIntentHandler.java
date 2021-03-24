package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import edu.hm.sweng.model.MyMemoSharingData;

import java.util.Arrays;
import java.util.Optional;

public class ToggleSharingRemindersIntentHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals("ToggleSharingRemindersIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        final String toggle = intentRequest.getIntent().getSlots().get("toggle").getValue();
        final MyMemoSharingData myMemoSharingData = new MyMemoSharingData(input.getAttributesManager());
        final String response;
        //if it contains part of deactivate, deaktiviere, ect.
        if (toggle.contains("dea") || toggle.contains("nicht")) {
            myMemoSharingData.disableSharing();
            response = "Deine reminder werden nicht mehr geteilt";
        } else {
            myMemoSharingData.enableSharing();
            String key = MyMemoSharingData.generateTempKey(input.getAttributesManager());
            key = Arrays.toString(key.toCharArray());
            key = key.substring(1, key.length() - 1);
            response = String.format("Dein Schluessel um deine reminder zu teilen lautet: %s, Noch einmal: %s", key, key);
        }
        return input.getResponseBuilder()
            .withSpeech(response)
            .withShouldEndSession(true)
            .build();
    }
}