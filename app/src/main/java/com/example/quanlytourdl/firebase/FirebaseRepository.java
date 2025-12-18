package com.example.quanlytourdl.firebase;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlytourdl.model.NhaCungCap; // ⭐ QUAN TRỌNG: Thêm Import Model mới
import com.example.quanlytourdl.model.Tour;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp; // ⭐ Dùng cho ngày đánh giá
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";

    // Tên các Collection
    private final String ALL_TOURS_COLLECTION = "Tours";
    private final String SUPPLIER_COLLECTION = "NhaCungCap"; // ⭐ Mới

    // Các trường dữ liệu
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_VEHICLE = "phuongTien";
    private static final String FIELD_HDV = "maHDV";

    // Các trường đánh giá hiệu suất (Khớp với model NhaCungCap)
    private static final String FIELD_PERF_SCORE = "diemHieuSuat";
    private static final String FIELD_PERF_COMMENT = "nhanXetHieuSuat";
    private static final String FIELD_PERF_DATE = "ngayDanhGia";

    private final FirebaseFirestore db;
    private final CollectionReference toursCollectionRef;
    private final CollectionReference suppliersCollectionRef; // ⭐ Mới

    public FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
        toursCollectionRef = db.collection(ALL_TOURS_COLLECTION);
        suppliersCollectionRef = db.collection(SUPPLIER_COLLECTION);
        Log.d(TAG, "Đã khởi tạo FirebaseRepository.");
    }

    // =========================================================================
    // PHẦN XỬ LÝ NHÀ CUNG CẤP (SUPPLIERS) ⭐ MỚI
    // =========================================================================

    /**
     * Lấy thông tin chi tiết một Nhà Cung Cấp theo ID.
     */
    public LiveData<NhaCungCap> getSupplierById(String nccId) {
        MutableLiveData<NhaCungCap> supplierLiveData = new MutableLiveData<>();

        if (nccId == null || nccId.isEmpty()) return supplierLiveData;

        suppliersCollectionRef.document(nccId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Lỗi khi lấy thông tin NCC:", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                NhaCungCap ncc = snapshot.toObject(NhaCungCap.class);
                if (ncc != null) {
                    ncc.setMaNhaCungCap(snapshot.getId());
                    supplierLiveData.setValue(ncc);
                }
            }
        });
        return supplierLiveData;
    }

    /**
     * Cập nhật đánh giá hiệu suất cho Nhà cung cấp.
     */
    public Task<Void> updateSupplierPerformance(String nccId, float score, String comment) {
        if (nccId == null || nccId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("ID Nhà cung cấp rỗng."));
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_PERF_SCORE, score);
        updates.put(FIELD_PERF_COMMENT, comment);
        updates.put(FIELD_PERF_DATE, Timestamp.now()); // Lưu thời điểm đánh giá hiện tại

        return suppliersCollectionRef.document(nccId).update(updates);
    }

    // =========================================================================
    // PHẦN XỬ LÝ TOUR (GIỮ NGUYÊN VÀ TỐI ƯU)
    // =========================================================================

    public LiveData<List<Tour>> getToursChoPheDuyet() {
        MutableLiveData<List<Tour>> toursLiveData = new MutableLiveData<>();

        toursCollectionRef.whereEqualTo(FIELD_STATUS, "CHO_PHE_DUYET")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Lỗi lắng nghe dữ liệu Tour chờ duyệt:", e);
                        toursLiveData.setValue(null);
                        return;
                    }

                    if (snapshots != null) {
                        List<Tour> tourList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : snapshots) {
                            Tour tour = document.toObject(Tour.class);
                            tour.setMaTour(document.getId());
                            tourList.add(tour);
                        }
                        toursLiveData.setValue(tourList);
                    }
                });
        return toursLiveData;
    }

    public Task<Void> updateTourStatus(String maTour, String newStatus) {
        if (maTour == null || maTour.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Mã Tour rỗng."));
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_STATUS, newStatus);

        return toursCollectionRef.document(maTour).update(updates);
    }

    public void removeTourListener() {
        // Có thể bổ sung quản lý ListenerRegistration tại đây nếu cần thiết
        Log.d(TAG, "Cleanup listeners called.");
    }
}