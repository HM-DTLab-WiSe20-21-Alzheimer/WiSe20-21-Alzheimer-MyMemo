package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelpIntentHandlerTest {

    private HandlerInput handlerInput;
    private HelpIntentHandler sut;

    @Before
    public void setUp() {
        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        sut = new HelpIntentHandler();
    }

    @Test
    public void canHandle() {
        IntentRequest intentRequest = IntentRequest.builder().withIntent(Intent.builder().withName("AMAZON.HelpIntent").build()).build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withRequest(intentRequest).build();
        final LaunchRequest launchRequest = LaunchRequest.builder().build();
        final boolean response = sut.canHandle(HandlerInput.builder().withRequestEnvelope(requestEnvelope).build(), intentRequest);
        assertTrue(response);
    }

    @Test
    public void handle() {
        final Optional<Response> response = sut.handle(handlerInput, IntentRequest.builder().build());

        assertTrue(response.isPresent());
    }
}