package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView; // Import SearchView
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.KhieuNaiAdapter;
import com.example.quanlytourdl.model.KhieuNai;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DanhSachKhieuNaiFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private SearchView searchView; // [MỚI]

    private KhieuNaiAdapter adapter;
    private List<KhieuNai> mListOriginal; // [MỚI] Danh sách gốc
    private List<KhieuNai> mListDisplay;  // [MỚI] Danh sách hiển thị (đã lọc)

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_khieu_nai, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupRecyclerView();

        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_list_kn);
        recyclerView = view.findViewById(R.id.rv_list_khieu_nai);
        fabAdd = view.findViewById(R.id.fab_add_khieu_nai);
        searchView = view.findViewById(R.id.sv_search_complaint); // [MỚI]

        // 1. Nút Back
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // 2. Nút Cộng
        fabAdd.setOnClickListener(v -> openFragment(new TaoKhieuNaiFragment()));

        // 3. [MỚI] Xử lý tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query); // Tìm khi nhấn Enter
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText); // Tìm ngay khi gõ phím (Realtime Search)
                return true;
            }
        });
    }

    // [MỚI] Hàm lọc danh sách
    private void filterList(String text) {
        mListDisplay.clear();
        if (text.isEmpty()) {
            // Nếu không nhập gì, hiển thị toàn bộ
            mListDisplay.addAll(mListOriginal);
        } else {
            // Chuẩn hóa chuỗi tìm kiếm (chữ thường)
            String searchText = text.toLowerCase();
            for (KhieuNai item : mListOriginal) {
                // Tìm theo Tên khách hàng HOẶC Mã phiếu
                if (item.getCustomerName().toLowerCase().contains(searchText) ||
                        item.getId().toLowerCase().contains(searchText)) {
                    mListDisplay.add(item);
                }
            }
        }
        // Cập nhật giao diện
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupRecyclerView() {
        mListOriginal = new ArrayList<>();
        mListDisplay = new ArrayList<>();

        // Adapter liên kết với mListDisplay (danh sách hiển thị)
        adapter = new KhieuNaiAdapter(mListDisplay, item -> {
            // Click xem chi tiết
            openDetailFragment(item);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load dữ liệu Realtime từ Firebase
        db.collection("Complaints")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        mListOriginal.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            KhieuNai item = doc.toObject(KhieuNai.class);
                            if (item != null) {
                                mListOriginal.add(item);
                            }
                        }
                        // Khi dữ liệu mới về, cập nhật lại bộ lọc hiện tại
                        // (Để nếu đang tìm kiếm dở thì vẫn giữ kết quả tìm kiếm)
                        String currentQuery = searchView.getQuery().toString();
                        filterList(currentQuery);
                    }
                });
    }

    // Hàm chuyển trang chi tiết
    private void openDetailFragment(KhieuNai item) {
        ChiTietKhieuNaiFragment fragment = new ChiTietKhieuNaiFragment();
        Bundle args = new Bundle();
        args.putString("ID", item.getId());
        args.putString("CUSTOMER", item.getCustomerName());
        args.putString("TOUR", item.getTourId());
        args.putString("DATE", item.getDateIncident());
        args.putString("CONTENT", item.getContent());
        args.putString("PRIORITY", item.getPriority());
        args.putString("STATUS", item.getStatus());
        args.putString("IMG_URI", item.getEvidenceUri());

        fragment.setArguments(args);
        openFragment(fragment);
    }

    private void openFragment(Fragment fragment) {
        View parentView = (View) getView().getParent();
        if (parentView != null) {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.replace(parentView.getId(), fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}