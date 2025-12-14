package com.example.quanlytourdl.model;

import java.util.List;

public class TourDaySchedule {
    public int dayNumber;
    public String title = "";
    public String summary = "";
    public List<TimelineEvent> detailedEvents;
    public TourDaySchedule() {
    }
    @Override
    public String toString() {
        return "Ngày " + dayNumber + ": " + summary;
    }
}