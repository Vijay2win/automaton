package com.automaton.myq;

import java.io.IOException;
import java.util.List;

import org.junit.*;

import com.automaton.accessories.GarageDoor.DoorState;
import com.automaton.server.AutomatonConfiguration;

public class MyQGarageDoorTest {
    private static final String USER = AutomatonConfiguration.getString("automaton.myq.user_name", "vijay2win@yahoo.com");
    private static final String PASSWORD = AutomatonConfiguration.getString("automaton.myq.password", "xxxxx");

    @Test
    @Ignore
    public void testLogin() throws IOException {
        GarageDoorHub door = new GarageDoorHub(USER, PASSWORD);
        List<GarageDoorDevice> doors = door.init();
        Assert.assertEquals(1, doors.size());
        door.open(doors.get(0));
        
        DoorState currentState = door.state(doors.get(0));
        DoorState state = door.state(doors.get(0));
        Assert.assertEquals(DoorState.OPENING, state);
    }
    
    @Test
    public void testConfig() throws IOException {
        boolean test = AutomatonConfiguration.getBoolean("test", true);
        Assert.assertEquals(Boolean.TRUE, test);
        
        boolean test1 = AutomatonConfiguration.getBoolean("test1", false);
        Assert.assertEquals(Boolean.FALSE, test1);
        
        boolean myq_enable = AutomatonConfiguration.getBoolean("automaton.hub.myq_enable", true);
        Assert.assertEquals(Boolean.TRUE, myq_enable);
    }
}
