package com.example.quanlytourdl.firebase;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class SupplierDeletionHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference supplierRef = db.collection("NhaCungCap"); // Thay thế bằng tên collection thực tế
    private final CollectionReference contractRef = db.collection("HopDong"); // Thay thế bằng tên collection thực tế

    /**
     * Xóa Nhà Cung Cấp và cập nhật tất cả các Hợp Đồng liên quan.
     * Thao tác được thực hiện trong một WriteBatch để đảm bảo tính nguyên vẹn (atomic).
     *
     * @param supplierId ID của Nhà Cung Cấp cần xóa.
     */
    public void deleteSupplierAndHandleContracts(String supplierId, DeletionCallback callback) {
        // Bắt đầu một Batch Write
        WriteBatch batch = db.batch();

        // 1. Đánh dấu Nhà Cung Cấp là đã xóa trong các Hợp Đồng liên quan
        contractRef.whereEqualTo("supplierId", supplierId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        // Cập nhật Hợp Đồng thay vì xóa hẳn (là cách an toàn hơn)
                        batch.update(document.getReference(), "tenNhaCungCap", "[Đã xóa]");
                        batch.update(document.getReference(), "supplierId", ""); // Xóa ID liên kết
                        batch.update(document.getReference(), "trangThai", "NC Cung Cấp Đã Xóa");
                    }

                    // 2. Xóa tài liệu Nhà Cung Cấp
                    batch.delete(supplierRef.document(supplierId));

                    // 3. Commit Batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess("Đã xóa Nhà Cung Cấp và cập nhật Hợp Đồng liên quan."))
                            .addOnFailureListener(e -> callback.onFailure("Lỗi khi Commit Batch: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi khi truy vấn Hợp Đồng: " + e.getMessage()));
    }

    public interface DeletionCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }
}