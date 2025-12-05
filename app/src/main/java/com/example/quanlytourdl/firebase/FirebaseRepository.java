package com.example.quanlytourdl.firebase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlytourdl.model.Tour;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRepository {

    private final DatabaseReference databaseReference;
    private final String TOUR_NODE = "tours_cho_phe_duyet";

    public FirebaseRepository() {
        // Khởi tạo Firebase Database và tham chiếu đến node "tours_cho_phe_duyet"
        databaseReference = FirebaseDatabase.getInstance().getReference(TOUR_NODE);
    }

    /**
     * Lấy danh sách Tour chờ phê duyệt từ Firebase.
     * Sử dụng LiveData để Fragment có thể quan sát (observe) sự thay đổi.
     */
    public LiveData<List<Tour>> getToursChoPheDuyet() {
        MutableLiveData<List<Tour>> toursLiveData = new MutableLiveData<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Tour> tourList = new ArrayList<>();
                for (DataSnapshot tourSnapshot : snapshot.getChildren()) {
                    // Ánh xạ dữ liệu từ Firebase vào lớp Tour
                    Tour tour = tourSnapshot.getValue(Tour.class);
                    if (tour != null) {
                        tourList.add(tour);
                    }
                }
                toursLiveData.setValue(tourList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi (ví dụ: in log hoặc gửi thông báo lỗi)
                // toursLiveData.setValue(null); // Tùy chọn: Đặt giá trị null nếu có lỗi
            }
        });
        return toursLiveData;
    }

    /**
     * Cập nhật trạng thái phê duyệt của Tour.
     * @param maTour Mã tour cần cập nhật
     * @param isApproved Trạng thái mới (true: Phê duyệt, false: Từ chối)
     */
    public void updateTourApprovalStatus(String maTour, boolean isApproved) {
        String status = isApproved ? "approved" : "rejected";
        databaseReference.child(maTour).child("trangThaiDuyet").setValue(status);
        // Lưu ý: Cần đảm bảo maTour là key (node con) trực tiếp trong "tours_cho_phe_duyet"
    }
}