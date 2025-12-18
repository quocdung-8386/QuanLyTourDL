package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton; // Import mới

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuanLyNhanSuFragment extends Fragment {

    private RecyclerView rvNhanSu;
    private NhanSuAdapter nhanSuAdapter;
    private List<NhanVien> mListNhanVien;
    private List<NhanVien> mListNhanVienFull;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private ImageButton btnBack; // Khai báo nút back
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_nhan_su, container, false);

        // 1. Ánh xạ View
        rvNhanSu = view.findViewById(R.id.rv_nhan_su);
        fabAdd = view.findViewById(R.id.fab_add);
        etSearch = view.findViewById(R.id.et_search);
        btnBack = view.findViewById(R.id.btn_back); // Ánh xạ nút Back

        db = FirebaseFirestore.getInstance();

        // 2. Thiết lập RecyclerView
        mListNhanVien = new ArrayList<>();
        mListNhanVienFull = new ArrayList<>();
        nhanSuAdapter = new NhanSuAdapter(mListNhanVien);

        rvNhanSu.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNhanSu.setAdapter(nhanSuAdapter);

        // 3. Logic chức năng
        listenToEmployeeData();
        setupSearch();

        // 4. Sự kiện Click
        fabAdd.setOnClickListener(v -> openFragment(new TaoTaiKhoanNhanVienFragment()));

        // --- XỬ LÝ NÚT BACK ---
        btnBack.setOnClickListener(v -> {
            // Cách 1: Chuyển trực tiếp sang CSKHFragment
            openFragment(new CSKHFragment());

            // Cách 2 (Nếu muốn quay lại màn hình trước đó trong lịch sử):
            // getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<NhanVien> filteredList = new ArrayList<>();
                for (NhanVien nv : mListNhanVienFull) {
                    boolean matchName = nv.getFullName() != null && nv.getFullName().toLowerCase().contains(query);
                    boolean matchId = nv.getId() != null && nv.getId().toLowerCase().contains(query);
                    boolean matchEmail = nv.getEmail() != null && nv.getEmail().toLowerCase().contains(query);
                    if (matchName || matchId || matchEmail) filteredList.add(nv);
                }
                nhanSuAdapter.setFilteredList(filteredList);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void listenToEmployeeData() {
        db.collection("Users").orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;
                    mListNhanVienFull.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String id = doc.getString("employeeCode");
                        if (id == null) id = doc.getId().substring(0, 4).toUpperCase();

                        mListNhanVienFull.add(new NhanVien(
                                doc.getId(),
                                doc.getString("fullName"),
                                id,
                                doc.getString("department"),
                                doc.getString("email"),
                                doc.getString("role")
                        ));
                    }
                    nhanSuAdapter.setFilteredList(new ArrayList<>(mListNhanVienFull));
                });
    }

    private void openFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}