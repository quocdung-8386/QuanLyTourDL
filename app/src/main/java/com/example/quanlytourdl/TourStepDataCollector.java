package com.example.quanlytourdl;

import com.example.quanlytourdl.model.Tour;

public interface TourStepDataCollector {


    boolean collectDataAndValidate(Tour tour);
}