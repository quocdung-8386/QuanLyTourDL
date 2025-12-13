package com.example.quanlytourdl.firebase;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlytourdl.model.Tour;
// SỬ DỤNG CÁC CLASS CỦA FIRESTORE
import com.google.android.gms.tasks.Task; // ⭐ ĐÃ IMPORT: Dùng để trả về Task
import com.google.android.gms.tasks.Tasks; // ⭐ ĐÃ IMPORT: Dùng để trả về Task thất bại
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";
    private final String ALL_TOURS_COLLECTION = "Tours";

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_VEHICLE = "phuongTien";
    private static final String FIELD_HDV = "maHDV";

    private static final String STATUS_PENDING = "CHO_PHE_DUYET";
    public static final String STATUS_APPROVED = "DANG_MO_BAN";
    public static final String STATUS_REJECTED = "DA_TU_CHOI";

    private static final List<String> STATUSES_AWAITING_ASSIGNMENT = Arrays.asList(
            STATUS_APPROVED,
            "DANG_CHO_PHAN_CONG"
    );

    private final FirebaseFirestore db;
    private final CollectionReference toursCollectionRef;

    public FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
        toursCollectionRef = db.collection(ALL_TOURS_COLLECTION);
        Log.d(TAG, "Đã tham chiếu đến Collection: " + ALL_TOURS_COLLECTION);
    }

    // =========================================================================
    // PHƯƠNG THỨC LẤY TOUR CHỜ GÁN
    // =========================================================================

    public LiveData<List<Tour>> getToursChoGan() {
        MutableLiveData<List<Tour>> toursLiveData = new MutableLiveData<>();

        Query query = toursCollectionRef
                .whereIn(FIELD_STATUS, STATUSES_AWAITING_ASSIGNMENT)
                .whereEqualTo(FIELD_VEHICLE, null);

        query.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Lỗi lắng nghe dữ liệu Tour chờ gán:", e);
                    toursLiveData.setValue(null);
                    return;
                }

                if (snapshots != null) {
                    List<Tour> tourList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshots) {
                        try {
                            Tour tour = document.toObject(Tour.class);
                            tour.setMaTour(document.getId());
                            tourList.add(tour);
                        } catch (Exception ex) {
                            Log.e(TAG, "LỖI ÁNH XẠ (MAPPING) Tour Document ID " + document.getId() + ": " + ex.getMessage());
                        }
                    }
                    toursLiveData.setValue(tourList);
                    Log.d(TAG, "Hoàn tất tải dữ liệu Tour chờ gán. Tổng số Tour hiển thị: " + tourList.size());
                }
            }
        });
        return toursLiveData;
    }


    // =========================================================================
    // PHƯƠNG THỨC LẤY TOUR CHỜ PHÊ DUYỆT
    // =========================================================================

    public LiveData<List<Tour>> getToursChoPheDuyet() {
        MutableLiveData<List<Tour>> toursLiveData = new MutableLiveData<>();

        Query query = toursCollectionRef.whereEqualTo(FIELD_STATUS, STATUS_PENDING);

        query.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Lỗi lắng nghe dữ liệu Tour chờ duyệt:", e);
                    toursLiveData.setValue(null);
                    return;
                }

                if (snapshots != null) {
                    List<Tour> tourList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshots) {
                        try {
                            Tour tour = document.toObject(Tour.class);
                            tour.setMaTour(document.getId());
                            tourList.add(tour);
                        } catch (Exception ex) {
                            Log.e(TAG, "LỖI ÁNH XẠ (MAPPING) Tour Document ID " + document.getId() + ": " + ex.getMessage());
                        }
                    }
                    toursLiveData.setValue(tourList);
                    Log.d(TAG, "Hoàn tất tải dữ liệu Tour chờ duyệt. Tổng số Tour hiển thị: " + tourList.size());
                }
            }
        });
        return toursLiveData;
    }

    // =========================================================================
    // PHƯƠNG THỨC CẬP NHẬT TRẠNG THÁI (TRẢ VỀ TASK)
    // =========================================================================

    /**
     * Cập nhật trạng thái phê duyệt của Tour.
     * ⭐ TRẢ VỀ Task<Void> (ĐÃ SỬA LỖI)
     */
    public Task<Void> updateTourStatus(String maTour, String newStatus) {
        if (maTour == null || maTour.isEmpty()) {
            Log.e(TAG, "Mã tour rỗng, không thể cập nhật.");
            // Trả về một Task thất bại
            return Tasks.forException(new IllegalArgumentException("Mã Tour rỗng."));
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_STATUS, newStatus);

        // Nếu hủy duyệt/từ chối, xóa các trường phân công
        if (newStatus.equals(STATUS_PENDING) || newStatus.equals(STATUS_REJECTED)) {
            updates.put(FIELD_VEHICLE, null);
            updates.put(FIELD_HDV, null);
        }

        return toursCollectionRef.document(maTour).update(updates);
    }

    // =========================================================================
    // PHƯƠNG THỨC DỌN DẸP LISTENER
    // =========================================================================

    /**
     * Phương thức dọn dẹp listener.
     */
    public void removeTourListener() {
        Log.w(TAG, "Phương thức removeTourListener() hiện không gỡ bỏ bất kỳ Listener toàn cục nào.");
    }
}