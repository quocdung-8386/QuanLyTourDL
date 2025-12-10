package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher; // Import quan trọng cho EditText
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText; // Import EditText
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

    // Hai list để xử lý tìm kiếm
    private List<KhachHang> listKhachHang;     // List hiển thị
    private List<KhachHang> fullListKhachHang; // List gốc (Backup)

    private ImageView btnAdd, btnBack, btnFilter;
    private EditText edtSearch; // <--- Đổi từ SearchView sang EditText

    private FirebaseFirestore db;
    private CollectionReference khachHangRef;
    private KhachHang khachHangToDelete = null;

    private String taoMaKhachHangTuDong() {
        long timestamp = System.currentTimeMillis();
        long shortId = timestamp % 1000000;
        return "KH" + shortId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        khachHangRef = db.collection("khachhang");

        // LẮNG NGHE THÊM MỚI
        getParentFragmentManager().setFragmentResultListener("add_customer_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String newName = result.getString("new_name");
                String newPhone = result.getString("new_phone");
                String newDob = result.getString("new_dob");
                String newEmail = result.getString("new_email");
                String customId = taoMaKhachHangTuDong();

                KhachHang newKH = new KhachHang(customId, newName, newPhone, newDob, newEmail, R.drawable.ic_launcher_background);
                khachHangRef.document(customId).set(newKH)
                        .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show());
            }
        });

        // LẮNG NGHE XÓA
        getParentFragmentManager().setFragmentResultListener("delete_customer_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean confirmed = result.getBoolean("confirm_delete");
                if (confirmed && khachHangToDelete != null) {
                    String idToDelete = khachHangToDelete.getId();
                    if (idToDelete != null) {
                        khachHangRef.document(idToDelete).delete()
                                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show());
                    }
                    khachHangToDelete = null;
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dskh, container, false); // Đảm bảo tên layout đúng

        // 1. Ánh xạ View theo XML mới
        rvKhachHang = view.findViewById(R.id.rvKhachHang);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnBack = view.findViewById(R.id.btnBack);

        edtSearch = view.findViewById(R.id.edtSearch); // <--- Ánh xạ EditText
        btnFilter = view.findViewById(R.id.btnFilter); // <--- Ánh xạ nút Filter

        // 2. Setup RecyclerView
        listKhachHang = new ArrayList<>();
        fullListKhachHang = new ArrayList<>();

        adapter = new KhachHangAdapter(listKhachHang,
                this::moManHinhChiTiet,
                (khachHang, position) -> {
                    khachHangToDelete = khachHang;
                    moManHinhXoa(khachHang);
                }
        );

        rvKhachHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKhachHang.setAdapter(adapter);

        // 3. Setup các chức năng
        getDataFromFirestore();
        setupSearch(); // <--- Hàm tìm kiếm mới

        // 4. Sự kiện Click
        btnAdd.setOnClickListener(v -> replaceFragment(new TaoHoSoKhachHangFragment()));

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Nút lọc (Hiện tại chưa có chức năng, để Toast tạm)
        btnFilter.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chức năng lọc nâng cao đang phát triển", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    // --- HÀM TÌM KIẾM CHO EDIT TEXT ---
    private void setupSearch() {
        if (edtSearch == null) return;

        // Dùng TextWatcher để lắng nghe thay đổi ký tự
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần dùng
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi nội dung thay đổi -> Gọi hàm lọc
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần dùng
            }
        });
    }

    private void filterList(String text) {
        listKhachHang.clear();
        if (text.isEmpty()) {
            listKhachHang.addAll(fullListKhachHang);
        } else {
            String textSearch = text.toLowerCase();
            for (KhachHang kh : fullListKhachHang) {
                // Tìm theo tên HOẶC số điện thoại
                if (kh.getTen().toLowerCase().contains(textSearch) ||
                        kh.getSdt().contains(textSearch)) {
                    listKhachHang.add(kh);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void getDataFromFirestore() {
        khachHangRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", "Lỗi data", error);
                    return;
                }
                if (value != null) {
                    listKhachHang.clear();
                    fullListKhachHang.clear();
                    for (DocumentSnapshot doc : value) {
                        KhachHang kh = doc.toObject(KhachHang.class);
                        if (kh != null) {
                            kh.setId(doc.getId());
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
        transaction.replace(R.id.main_content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void moManHinhChiTiet(KhachHang kh) {
        ChiTietKhachHangFragment fragmentChiTiet = new ChiTietKhachHangFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", kh.getId());
        bundle.putString("name", kh.getTen());
        bundle.putString("phone", kh.getSdt());
        bundle.putString("dob", kh.getNgaySinh());
        bundle.putInt("avatar", kh.getAvatarResId());
        bundle.putString("email", kh.getEmail());
        fragmentChiTiet.setArguments(bundle);
        replaceFragment(fragmentChiTiet);
    }

    private void moManHinhXoa(KhachHang kh) {
        XoaKhachHangFragment fragmentXoa = new XoaKhachHangFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", kh.getTen());
        fragmentXoa.setArguments(bundle);
        replaceFragment(fragmentXoa);
    }
}