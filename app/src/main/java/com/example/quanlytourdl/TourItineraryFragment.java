package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.adapter.TimelineAdapter;
import com.example.quanlytourdl.model.TimelineEvent;
import com.example.quanlytourdl.firebase.ItineraryParser;

import java.util.Collections;
import java.util.List;

public class TourItineraryFragment extends Fragment {

    private static final String TAG = "TourItineraryFragment";
    private static final String ARG_ITINERARY_DETAILS = "itinerary_details";

    private String itineraryDetailsJson;
    private RecyclerView timelineRecyclerView;
    private ImageView imgMapPreview;
    private Button btnViewMap;
    private TimelineAdapter timelineAdapter; // ⭐ Khai báo adapter ở cấp độ class

    public static TourItineraryFragment newInstance(String itineraryDetailsJson) {
        TourItineraryFragment fragment = new TourItineraryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITINERARY_DETAILS, itineraryDetailsJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itineraryDetailsJson = getArguments().getString(ARG_ITINERARY_DETAILS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả định layout file: fragment_tour_itinerary.xml
        View view = inflater.inflate(R.layout.fragment_tour_itinerary, container, false);

        timelineRecyclerView = view.findViewById(R.id.recycler_timeline);
        imgMapPreview = view.findViewById(R.id.img_map_preview);
        btnViewMap = view.findViewById(R.id.btn_view_map);

        setupRecyclerView();
        loadItineraryData(); // ⭐ Tách logic tải/phân tích dữ liệu

        btnViewMap.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở bản đồ tuyến đường chi tiết", Toast.LENGTH_SHORT).show();
            // TODO: Triển khai mở ứng dụng bản đồ hoặc Fragment Map
        });

        return view;
    }

    private void setupRecyclerView() {
        // Khởi tạo adapter với danh sách rỗng ban đầu, sử dụng requireContext()
        timelineAdapter = new TimelineAdapter(requireContext(), Collections.emptyList());
        timelineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        timelineRecyclerView.setAdapter(timelineAdapter);
    }

    /**
     * Tải và phân tích dữ liệu lịch trình JSON (được lấy từ Firestore/Fragment cha).
     */
    private void loadItineraryData() {
        if (itineraryDetailsJson == null || itineraryDetailsJson.trim().isEmpty()) {
            Log.w(TAG, "Dữ liệu lịch trình JSON rỗng hoặc null.");
            Toast.makeText(getContext(), "Không có dữ liệu lịch trình để hiển thị.", Toast.LENGTH_LONG).show();
            return;
        }

        List<TimelineEvent> events = Collections.emptyList();

        try {
            Log.d(TAG, "Bắt đầu phân tích lịch trình JSON...");
            // ⭐ Gọi ItineraryParser để phân tích JSON
            events = ItineraryParser.parse(itineraryDetailsJson);

        } catch (Exception e) {
            Log.e(TAG, "LỖI PHÂN TÍCH CÚ PHÁP LỊCH TRÌNH JSON:", e);
            Toast.makeText(getContext(), "Lỗi tải lịch trình chi tiết. Vui lòng kiểm tra định dạng dữ liệu.", Toast.LENGTH_LONG).show();
        }

        // Cập nhật adapter với danh sách sự kiện đã phân tích
        if (timelineAdapter != null) {
            timelineAdapter.updateList(events);
            if (events.isEmpty()) {
                Log.w(TAG, "Phân tích cú pháp thành công nhưng không có sự kiện nào được tìm thấy.");
            }
        } else {
            Log.e(TAG, "TimelineAdapter chưa được khởi tạo.");
        }
    }
}