package com.java.lld.oops.elevatorcontrolsystem.model;

import com.java.lld.oops.elevatorcontrolsystem.model.enums.Direction;
import com.java.lld.oops.elevatorcontrolsystem.model.enums.ElevatorRequestType;

public class ElevatorRequest {
    private Long requestId;
    private ElevatorRequestType requestType;
    private Direction direction;
    private Floor sourceFloor;
    private Floor destinationFloor;
}
