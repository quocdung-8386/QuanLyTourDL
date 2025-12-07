package com.example.quanlytourdl.firebase;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlytourdl.model.Tour;
// SỬ DỤNG CÁC CLASS CỦA FIRESTORE
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration; // Dùng để quản lý listener real-time

import java.util.ArrayList;
import java.util.List;

public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";
    // Tên Collection gốc chứa TẤT CẢ các tour (Giả định là "Tours" như bạn đã dùng)
    private final String ALL_TOURS_COLLECTION = "Tours";

    // Hằng số trạng thái
    private static final String FIELD_STATUS = "status";
    private static final String STATUS_PENDING = "CHO_PHE_DUYET";
    public static final String STATUS_APPROVED = "DANG_MO_BAN";
    public static final String STATUS_REJECTED = "DA_TU_CHOI";

    // Tham chiếu Firestore
    private final FirebaseFirestore db;
    private final CollectionReference toursCollectionRef;
    private ListenerRegistration tourListenerRegistration; // Dùng để quản lý lắng nghe real-time

    public FirebaseRepository() {
        // KHỞI TẠO FIRESTORE
        db = FirebaseFirestore.getInstance();
        toursCollectionRef = db.collection(ALL_TOURS_COLLECTION);
        Log.d(TAG, "Đã tham chiếu đến Collection: " + ALL_TOURS_COLLECTION);
    }

    /**
     * Lấy danh sách Tour chờ phê duyệt bằng truy vấn Firestore.
     */
    public LiveData<List<Tour>> getToursChoPheDuyet() {
        MutableLiveData<List<Tour>> toursLiveData = new MutableLiveData<>();

        // 1. TẠO TRUY VẤN LỌC CHÍNH XÁC: Chỉ lấy các tour có status = "CHO_PHE_DUYET"
        Query query = toursCollectionRef.whereEqualTo(FIELD_STATUS, STATUS_PENDING);

        // 2. Thiết lập Listener real-time
        tourListenerRegistration = query.addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
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
                            // Chuyển đổi Document thành đối tượng Tour
                            Tour tour = document.toObject(Tour.class);
                            // Gán ID document (là MaTour) vào đối tượng
                            tour.setMaTour(document.getId());
                            tourList.add(tour);
                        } catch (Exception ex) {
                            Log.e(TAG, "LỖI ÁNH XẠ (MAPPING) Tour Document ID " + document.getId() + ": " + ex.getMessage());
                        }
                    }
                    toursLiveData.setValue(tourList);
                    Log.d(TAG, "Hoàn tất tải dữ liệu. Tổng số Tour hiển thị: " + tourList.size());
                }
            }
        });
        return toursLiveData;
    }

    /**
     * Cập nhật trạng thái phê duyệt của Tour (Sử dụng Firestore).
     */
    public void updateTourStatus(String maTour, String newStatus) {
        if (maTour == null || maTour.isEmpty()) {
            Log.e(TAG, "Mã tour rỗng, không thể cập nhật.");
            return;
        }

        // Cập nhật trường "status" trong Document có ID là maTour
        toursCollectionRef.document(maTour).update(FIELD_STATUS, newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật trạng thái tour " + maTour + " thành công, status: " + newStatus);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật trạng thái tour " + maTour + ": " + e.getMessage());
                });
    }

    /**
     * Phương thức dọn dẹp listener, nên được gọi khi Fragment/Activity bị hủy
     * để tránh rò rỉ bộ nhớ.
     */
    public void removeTourListener() {
        if (tourListenerRegistration != null) {
            tourListenerRegistration.remove();
            Log.d(TAG, "Đã gỡ bỏ Listener Tour.");
        }
    }
}