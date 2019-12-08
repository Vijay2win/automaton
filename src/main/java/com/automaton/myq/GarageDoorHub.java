package com.automaton.myq;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonProcessingException;
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
    private static final String WEBSITE = "https://api.myqdevice.com/api/v5";
    private static final String DEFAULT_APP_ID = "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu";

    private final Client client = Client.create();
    private final String userName;
    private final String password;

    private String sercurityToken;
    private String accountId;

    public GarageDoorHub(String username, String password) {
        this.userName = username;
        this.password = password;
    }

    protected List<GarageDoorDevice> init() throws IOException {
        this.sercurityToken = getSecurityToken();
        this.accountId = getAccountID();
        return getAllStatus();
    }

    private String getSecurityToken() throws IOException {
        WebResource resource = client.resource(WEBSITE).path("login");
        ClientResponse resp = headers(resource).post(ClientResponse.class,
                MAPPER.writeValueAsString(new HashMap<String, String>() {
                    {
                        put("Username", userName);
                        put("Password", password);
                    }
                }));
        return toMap(resp).get("SecurityToken").toString();
    }

    private String getAccountID() throws JsonProcessingException, IOException {
        WebResource resource = client.resource(WEBSITE).path("My").queryParam("expand", "account");
        ClientResponse resp = headers(resource).get(ClientResponse.class);
        return (String) ((Map) toMap(resp).get("Account")).get("Id");
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
            logger.error("issue in response, ", e);
        }

        // Default response
        return new HashMap<>();
    }

    private Builder headers(WebResource resource) {
        Builder builder = resource.getRequestBuilder().type(MediaType.APPLICATION_JSON);
        return builder.header("MyQApplicationId", DEFAULT_APP_ID)
                .header("SecurityToken", sercurityToken);
    }

    public List<GarageDoorDevice> getAllStatus() throws JsonProcessingException, IOException {
        WebResource resource = client.resource(WEBSITE).path("Accounts").path(accountId).path("Devices");
        ClientResponse resp = headers(resource).get(ClientResponse.class);
        
        Map response = toMap(resp);
        if (!response.containsKey("items")) {
            logger.error("No devices found in the response...");
            return new ArrayList<>();
        }

        List<Map<?, ?>> nodes = (List<Map<?, ?>>) response.get("items");
        logger.debug("Chamberlain MyQ Devices:");
        return nodes.stream()
                .map(n -> GarageDoorDevice.parse(n, GarageDoorHub.this))
                .filter(door -> door.deviceType.equals("virtualgaragedooropener"))
                .collect(Collectors.toList());
    }

    public void updateState(GarageDoorDevice device) {
        try {
            for (GarageDoorDevice d : getAllStatus()) {
                if (device.getSerialNumber().equals(d.getSerialNumber())) {
                    device.state = d.state;
                    logger.info("now checking status for {} and status {}", device.serialNumber, device.state);
                }
            }
        } catch (Throwable th) {
            logger.info("Error in parsing the status, ", th);
            try {
                this.sercurityToken = getSecurityToken();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void open(GarageDoorDevice device) {
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