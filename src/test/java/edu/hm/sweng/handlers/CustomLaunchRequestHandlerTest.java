package edu.hm.sweng.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
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

public class CustomLaunchRequestHandlerTest {

    private HandlerInput handlerInput;
    private CustomLaunchRequestHandler sut;

    @Before
    public void setUp() {
        handlerInput = mock(HandlerInput.class);
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());

        sut = new CustomLaunchRequestHandler();
    }

    @Test
    public void canHandle() {
        RequestEnvelope requestEnvelope = RequestEnvelope.builder()
            .withRequest(LaunchRequest.builder()
                             .build()).build();
        final LaunchRequest launchRequest = LaunchRequest.builder().build();
        final boolean response = sut.canHandle(HandlerInput.builder().withRequestEnvelope(requestEnvelope).build(), launchRequest);
        assertTrue(response);
    }

    @Test
    public void handle() {
        final Optional<Response> response = sut.handle(handlerInput, LaunchRequest.builder().build());

        assertTrue(response.isPresent());
    }
}