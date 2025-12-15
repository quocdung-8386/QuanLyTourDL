package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.HoanTienAdapter;
import com.example.quanlytourdl.model.HoanTien;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DanhSachHoanTienFragment extends Fragment {

    private RecyclerView rvDanhSachHoanTien;
    private HoanTienAdapter adapter;
    private List<HoanTien> listHoanTien;
    private FirebaseFirestore db;
    private ImageView btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_hoan_tien, container, false);

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Init Views
        LinearLayout btnXemChinhSach = view.findViewById(R.id.btnXemChinhSach);
        rvDanhSachHoanTien = view.findViewById(R.id.rvDanhSachHoanTien);

        // Setup RecyclerView
        listHoanTien = new ArrayList<>();
        adapter = new HoanTienAdapter(listHoanTien, new HoanTienAdapter.OnActionClickListener() {
            @Override
            public void onApprove(HoanTien item) {
                updateStatus(item.getId(), "da_hoan_tien");
            }

            @Override
            public void onReject(HoanTien item) {
                updateStatus(item.getId(), "da_tu_choi");
            }
        });

        rvDanhSachHoanTien.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDanhSachHoanTien.setAdapter(adapter);
        btnBack = view.findViewById(R.id.btnBack);

        // Load Data
        loadDataFromFirebase();

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
        // Sự kiện chuyển màn hình
        btnXemChinhSach.setOnClickListener(v -> {
            Fragment chinhSachFragment = new ChinhSachFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_frame, chinhSachFragment); // Lưu ý: ID container phải đúng
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void loadDataFromFirebase() {
        // Lắng nghe thay đổi thực (Realtime updates)
        db.collection("HoanTien") // Đảm bảo tên Collection trên Firebase đúng là "HoanTien"
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore", "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            listHoanTien.clear();
                            for (DocumentSnapshot doc : value) {
                                HoanTien item = doc.toObject(HoanTien.class);
                                if (item != null) {
                                    item.setId(doc.getId()); // Gán ID document vào object
                                    listHoanTien.add(item);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void updateStatus(String docId, String newStatus) {
        if (docId == null) return;

        db.collection("HoanTien").document(docId)
                .update("trangThai", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã cập nhật: " + newStatus, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
    }
}