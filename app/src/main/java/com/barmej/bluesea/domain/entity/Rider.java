package com.barmej.bluesea.domain.entity;

import java.io.Serializable;

public class Rider implements Serializable {
    private String id;
    private String assignedTrip = "";

    public Rider() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignedTrip() {
        return assignedTrip;
    }

    public void setAssignedTrip(String assignedTrip) {
        this.assignedTrip = assignedTrip;
    }
}
