package com.automaton.pairing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.automaton.http.HttpResponse;
import com.automaton.http.HttpResponses;
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
        TypeLengthValueUtils.DecodeResult d = TypeLengthValueUtils
                .decode(AutomatonUtils.readAllRemaining(request.content()));

        int method = d.getByte(PairingManager.MessageType.METHOD);
        if (method == 3) {
            byte[] username = d.getBytes(PairingManager.MessageType.USERNAME);
            byte[] ltpk = d.getBytes(PairingManager.MessageType.PUBLIC_KEY);
            Authenticator.INSTANCE.createUser(new String(username, StandardCharsets.UTF_8), ltpk);
        } else if (method == 4) {
            byte[] username = d.getBytes(PairingManager.MessageType.USERNAME);
            Authenticator.INSTANCE.removeUser(new String(username, StandardCharsets.UTF_8));
            this.advertiser.setDiscoverable(true);
        } else {
            throw new RuntimeException("Unrecognized method: " + method);
        }
        return (HttpResponse) new HttpResponses.PairingResponse(new byte[] { 6, 1, 2 });
    }
}
