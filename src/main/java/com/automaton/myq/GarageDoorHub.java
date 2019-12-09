package com.automaton.myq;

import java.io.IOError;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automaton.accessories.GarageDoor.DoorState;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.WebResource.Builder;

@SuppressWarnings({"serial", "unchecked"})
public class GarageDoorHub {
    private static final Logger logger = LoggerFactory.getLogger(GarageDoorHub.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String DEFAULT_APP_ID_KEY = "MyQApplicationId";
    private static final String SECURITY_TOKEN_KEY = "SecurityToken";

    private static final String WEBSITE = "https://api.myqdevice.com/api/v5";
    private static final String DEFAULT_APP_ID = "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu";

    private final Client client = Client.create();
    private final String userName;
    private final String password;
    private String accountId;

    private volatile String sercurityToken;

    public GarageDoorHub(String username, String password) {
        this.userName = username;
        this.password = password;
    }

    protected List<GarageDoorDevice> init() throws IOException {
        this.sercurityToken = getSecurityToken();
        this.accountId = getAccountID();
        return getAllStatus();
    }

    private String getSecurityToken() {
        try {
            WebResource resource = client.resource(WEBSITE).path("login");
            ClientResponse resp = resource.type(MediaType.APPLICATION_JSON)
                    .header(DEFAULT_APP_ID_KEY, DEFAULT_APP_ID)
                    .post(ClientResponse.class,
                    MAPPER.writeValueAsString(new HashMap<String, String>() {
                        {
                            put("Username", userName);
                            put("Password", password);
                        }
                    }));
            return toMap(resp).get(SECURITY_TOKEN_KEY).toString();
        } catch (IOException wtf) {
            throw new IOError(wtf);
        }
    }

    private String getAccountID() {
        WebResource resource = client.resource(WEBSITE).path("My").queryParam("expand", "account");
        ClientResponse resp = headers(resource).get(ClientResponse.class);
        return ((Map<?, ?>) toMap(resp).get("Account")).get("Id").toString();
    }

    private Map<?, ?> toMap(ClientResponse resp) {
        try {
            if (resp.getStatus() == 204) {
                // do nothing to parse the response
            } else if (resp.getStatus() == 200) {
                String json = resp.getEntity(String.class);
                return MAPPER.readValue(json, Map.class);
            } else {
                throw new IOException("Failed : HTTP error code : " + resp.getStatus());
            }
        } catch (Throwable e) {
            // TODO may be re-throw?
            logger.error("issue in response, security token, {} &  accountID {}", this.sercurityToken, this.accountId, e);
        }

        // Default response
        return new HashMap<>();
    }

    private Builder headers(WebResource resource) {
        Builder builder = resource.getRequestBuilder().type(MediaType.APPLICATION_JSON);
        return builder.header(DEFAULT_APP_ID_KEY, DEFAULT_APP_ID)
                .header(SECURITY_TOKEN_KEY, sercurityToken);
    }

    public List<GarageDoorDevice> getAllStatus() {
        WebResource resource = client.resource(WEBSITE).path("Accounts").path(accountId).path("Devices");
        ClientResponse resp = headers(resource).get(ClientResponse.class);
        
        Map<?, ?> response = toMap(resp);
        if (!response.containsKey("items")) {
            
            logger.error("No devices found in the response, re athenticating to make sure.");
            this.sercurityToken = getSecurityToken();
            this.accountId = getAccountID();
            return new ArrayList<>();
        }

        List<Map<?, ?>> nodes = (List<Map<?, ?>>) response.get("items");
        logger.debug("Chamberlain MyQ Devices:");
        return nodes.stream()
                .map(n -> GarageDoorDevice.parse(n, GarageDoorHub.this))
                .filter(door -> door.deviceType.equals("virtualgaragedooropener"))
                .collect(Collectors.toList());
    }

    public DoorState state(GarageDoorDevice device) {
        try {
            for (GarageDoorDevice d : getAllStatus()) {
                if (device.getSerialNumber().equals(d.getSerialNumber())) {
                    logger.info("now checking status for {} and status {}", d.serialNumber, d.state);
                    return d.state;
                }
            }
        } catch (Throwable th) {
            logger.info("Error in parsing the status, ", th);
        }
        return DoorState.STOPPED;
    }

    public void open(GarageDoorDevice device) {
        this.sercurityToken = getSecurityToken();
        this.accountId = getAccountID();

        WebResource resource = client.resource(WEBSITE).path("Accounts").path(accountId).path("Devices")
                .path(device.serialNumber).path("actions");
        ClientResponse resp = headers(resource).put(ClientResponse.class, new HashMap<String, String>() {
            {
                put("action_type", "open");
            }
        });
        device.state = DoorState.OPENING;
        logger.info("open response is {}", toMap(resp));
    }

    public void close(GarageDoorDevice device) {
        WebResource resource = client.resource(WEBSITE).path("Accounts").path(accountId).path("Devices")
                .path(device.serialNumber).path("actions");
        ClientResponse resp = headers(resource).put(ClientResponse.class, new HashMap<String, String>() {
            {
                put("action_type", "close");
            }
        });
        device.state = DoorState.CLOSING;
        logger.info("close response is {}", toMap(resp));
    }
}