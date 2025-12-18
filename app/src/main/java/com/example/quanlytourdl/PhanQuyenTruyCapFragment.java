package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

// --- QUAN TRỌNG: Import Adapter từ package con ---
import com.example.quanlytourdl.adapter.PhanQuyenTruyCapAdapter;

import java.util.ArrayList;
import java.util.List;

public class PhanQuyenTruyCapFragment extends Fragment {

    private RecyclerView rvNhanVien;
    private PhanQuyenTruyCapAdapter adapter;
    private List<NhanVien> mListGoc;
    private List<NhanVien> mListHienThi;
    private FirebaseFirestore db;
    private TextView tvTotalCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phan_quyen_truy_cap, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);

        mListGoc = new ArrayList<>();
        mListHienThi = new ArrayList<>();

        adapter = new PhanQuyenTruyCapAdapter(mListHienThi);
        rvNhanVien.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNhanVien.setAdapter(adapter);

        loadUsersFromFirebase();

        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        rvNhanVien = view.findViewById(R.id.rv_employees);
        EditText etSearch = view.findViewById(R.id.et_search);
        FloatingActionButton fabSave = view.findViewById(R.id.fab_save);
        tvTotalCount = view.findViewById(R.id.tv_total_count);

        toolbar.setNavigationOnClickListener(v -> {
            if (isAdded() && getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabSave.setOnClickListener(v -> saveAllPermissions());
    }

    private void loadUsersFromFirebase() {
        db.collection("Users").orderBy("fullName", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    mListGoc.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        NhanVien nv = doc.toObject(NhanVien.class);
                        if (nv != null) {
                            nv.setDocumentId(doc.getId());
                            mListGoc.add(nv);
                        }
                    }

                    if (tvTotalCount != null) {
                        tvTotalCount.setText("Tổng số nhân viên: " + mListGoc.size());
                    }

                    filterData("");
                });
    }

    private void filterData(String keyword) {
        mListHienThi.clear();
        if (keyword.isEmpty()) {
            mListHienThi.addAll(mListGoc);
        } else {
            for (NhanVien nv : mListGoc) {
                if (nv.getFullName() != null && nv.getFullName().toLowerCase().contains(keyword.toLowerCase())) {
                    mListHienThi.add(nv);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void saveAllPermissions() {
        if (mListGoc.isEmpty()) return;

        WriteBatch batch = db.batch();
        for (NhanVien nv : mListGoc) {
            String docId = nv.getDocumentId();
            if (docId != null) {
                batch.update(db.collection("Users").document(docId),
                        "accessTour", nv.isAccessTour(),
                        "accessCustomer", nv.isAccessCustomer(),
                        "accessReport", nv.isAccessReport()
                );
            }
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Đã cập nhật phân quyền!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}