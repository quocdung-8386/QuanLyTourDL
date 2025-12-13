package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.KhachHangAdapter;
import com.example.quanlytourdl.model.KhachHang;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DanhSachKhachHangFragment extends Fragment {

    private RecyclerView rvKhachHang;
    private KhachHangAdapter adapter;

    // List hiển thị và List gốc (Backup cho search)
    private List<KhachHang> listKhachHang;
    private List<KhachHang> fullListKhachHang;

    private ImageView btnAdd, btnBack, btnFilter;
    private EditText edtSearch;

    private FirebaseFirestore db;
    private CollectionReference khachHangRef;
    private KhachHang khachHangToDelete = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        khachHangRef = db.collection("khachhang");

        // LẮNG NGHE XÓA (Giữ nguyên logic này)
        getParentFragmentManager().setFragmentResultListener("delete_customer_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean confirmed = result.getBoolean("confirm_delete");
                if (confirmed && khachHangToDelete != null) {
                    String idToDelete = khachHangToDelete.getId();
                    if (idToDelete != null) {
                        khachHangRef.document(idToDelete).delete()
                                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Đã xóa khách hàng", Toast.LENGTH_SHORT).show());
                    }
                    khachHangToDelete = null;
                }
            }
        });

        // LƯU Ý: Đã xóa phần lắng nghe "add_customer_request" vì màn hình Tạo Hồ Sơ đã tự lưu vào Firestore.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dskh, container, false);

        // 1. Ánh xạ View
        rvKhachHang = view.findViewById(R.id.rvKhachHang);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnBack = view.findViewById(R.id.btnBack);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);

        // 2. Setup RecyclerView
        listKhachHang = new ArrayList<>();
        fullListKhachHang = new ArrayList<>();

        adapter = new KhachHangAdapter(listKhachHang,
                this::moManHinhChiTiet, // Sự kiện Click Item
                (khachHang, position) -> { // Sự kiện Click Delete
                    khachHangToDelete = khachHang;
                    moManHinhXoa(khachHang);
                }
        );

        rvKhachHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKhachHang.setAdapter(adapter);

        // 3. Lấy dữ liệu Realtime
        getDataFromFirestore();

        // 4. Setup Tìm kiếm
        setupSearch();

        // 5. Sự kiện Click
        btnAdd.setOnClickListener(v -> replaceFragment(new TaoHoSoKhachHangFragment()));

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnFilter.setOnClickListener(v ->
                Toast.makeText(getContext(), "Tính năng lọc đang phát triển", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void setupSearch() {
        if (edtSearch == null) return;
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList(String text) {
        listKhachHang.clear();
        if (text.isEmpty()) {
            listKhachHang.addAll(fullListKhachHang);
        } else {
            String textSearch = text.toLowerCase();
            for (KhachHang kh : fullListKhachHang) {
                // Tìm theo tên hoặc SĐT
                boolean matchName = kh.getTen() != null && kh.getTen().toLowerCase().contains(textSearch);
                boolean matchPhone = kh.getSdt() != null && kh.getSdt().contains(textSearch);

                if (matchName || matchPhone) {
                    listKhachHang.add(kh);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void getDataFromFirestore() {
        // addSnapshotListener giúp tự động cập nhật list khi có thay đổi (Thêm/Sửa/Xóa) trên DB
        khachHangRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error);
                    return;
                }
                if (value != null) {
                    listKhachHang.clear();
                    fullListKhachHang.clear();
                    for (DocumentSnapshot doc : value) {
                        KhachHang kh = doc.toObject(KhachHang.class);
                        if (kh != null) {
                            kh.setId(doc.getId()); // Quan trọng: Lấy ID document gán vào Object
                            listKhachHang.add(kh);
                            fullListKhachHang.add(kh);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_frame, fragment); // Đảm bảo ID này đúng với Activity
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void moManHinhChiTiet(KhachHang kh) {
        ChiTietKhachHangFragment fragmentChiTiet = new ChiTietKhachHangFragment();
        Bundle bundle = new Bundle();

        // Truyền ID quan trọng nhất để màn hình kia có thể fetch lại hoặc update
        bundle.putString("id", kh.getId());

        // Truyền dữ liệu hiển thị nhanh
        bundle.putString("name", kh.getTen());
        bundle.putString("phone", kh.getSdt());
        bundle.putString("dob", kh.getNgaySinh());
        bundle.putString("email", kh.getEmail());

        // Truyền các trường bổ sung (Phải khớp với Model mới)
        bundle.putString("address", kh.getDiaChi());
        bundle.putString("gender", kh.getGioiTinh());
        bundle.putString("cccd", kh.getCccd());
        bundle.putString("nationality", kh.getQuocTich());

        fragmentChiTiet.setArguments(bundle);
        replaceFragment(fragmentChiTiet);
    }

    private void moManHinhXoa(KhachHang kh) {
        // Giả sử bạn có XoaKhachHangFragment (Dialog Fragment)
        XoaKhachHangFragment fragmentXoa = new XoaKhachHangFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", kh.getTen());
        fragmentXoa.setArguments(bundle);

        replaceFragment(fragmentXoa);
    }
}