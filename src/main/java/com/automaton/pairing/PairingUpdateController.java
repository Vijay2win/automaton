package com.automaton.pairing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.automaton.http.HttpResponse;
import com.automaton.http.HttpResponses.PairingResponse;
import com.automaton.pairing.PairingManager.MessageType;
import com.automaton.pairing.TypeLengthValueUtils.DecodeResult;
import com.automaton.security.Authenticator;
import com.automaton.security.JmdnsHomekitAdvertiser;
import com.automaton.utils.AutomatonUtils;

import io.netty.handler.codec.http.FullHttpRequest;

public class PairingUpdateController {
    private final JmdnsHomekitAdvertiser advertiser;

    public PairingUpdateController(JmdnsHomekitAdvertiser advertiser) {
        this.advertiser = advertiser;
    }

    public HttpResponse handle(FullHttpRequest request) throws IOException {
        DecodeResult d = TypeLengthValueUtils.decode(AutomatonUtils.readAllRemaining(request.content()));

        int method = d.getByte(MessageType.METHOD);
        if (method == 3) { // Add pairing
            byte[] username = d.getBytes(MessageType.USERNAME);
            byte[] ltpk = d.getBytes(MessageType.PUBLIC_KEY);
            Authenticator.INSTANCE.createUser(new String(username, StandardCharsets.UTF_8), ltpk);
        } else if (method == 4) { // Remove pairing
            byte[] username = d.getBytes(MessageType.USERNAME);
            Authenticator.INSTANCE.removeUser(new String(username, StandardCharsets.UTF_8));
            advertiser.setDiscoverable(true);
        } else {
            throw new RuntimeException("Unrecognized method: " + method);
        }
        return new PairingResponse(new byte[] { 0x06, 0x01, 0x02 });
    }
}
