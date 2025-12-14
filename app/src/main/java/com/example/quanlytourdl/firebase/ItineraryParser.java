package com.example.quanlytourdl.firebase;

import com.example.quanlytourdl.model.TimelineEvent;
import com.example.quanlytourdl.model.TourDaySchedule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItineraryParser {

    private static final Gson gson = new Gson();

    public static List<TimelineEvent> parse(String itineraryDetailsJson) throws Exception {
        if (itineraryDetailsJson == null || itineraryDetailsJson.isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<TourDaySchedule>>() {}.getType();

        List<TourDaySchedule> dailySchedules;
        try {
            dailySchedules = gson.fromJson(itineraryDetailsJson, listType);
        } catch (Exception e) {
            throw new Exception("Lỗi phân tích cú pháp JSON lịch trình Tour: " + e.getMessage(), e);
        }

        if (dailySchedules == null) {
            return new ArrayList<>();
        }

        // Tạo danh sách phẳng (Flatten) các sự kiện
        List<TimelineEvent> flatEvents = new ArrayList<>();

        for (TourDaySchedule schedule : dailySchedules) {
            if (schedule.summary != null && !schedule.summary.isEmpty()) {
                // Thêm một sự kiện Tiêu đề Ngày (Day Header)
                flatEvents.add(new TimelineEvent(
                        null, // Không có thời gian
                        String.format("Ngày %d: %s", schedule.dayNumber, schedule.summary),
                        null,
                        "day_header", // Giả định icon cho tiêu đề ngày
                        null
                ));
            }

            // Thêm các sự kiện chi tiết của ngày đó
            if (schedule.detailedEvents != null) {
                flatEvents.addAll(schedule.detailedEvents);
            }
        }

        return flatEvents;
    }

    /**
     * Chuyển đổi List<TourDaySchedule> thành chuỗi JSON để lưu trữ trong Firestore.
     */
    public static String serialize(List<TourDaySchedule> scheduleList) {
        Type listType = new TypeToken<List<TourDaySchedule>>() {}.getType();
        return gson.toJson(scheduleList, listType);
    }
}