package com.java.lld.oops.elevatorcontrolsystem.model;


import com.java.lld.oops.elevatorcontrolsystem.model.enums.Direction;
import com.java.lld.oops.elevatorcontrolsystem.model.enums.DoorState;
import com.java.lld.oops.elevatorcontrolsystem.model.enums.ElevatorState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Elevator {
    private int id;
    private int currentFloor;
    private ElevatorState elevatorState;
    private DoorState doorState;
    private List<ElevatorRequest> requestList;
    private Direction direction;
}
