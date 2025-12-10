package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<KhachHang> listKhachHang;

    // Lưu ý: Kiểm tra file XML của bạn xem nút thêm là ImageView hay TextView để khai báo cho đúng
    private ImageView btnAdd;
    private ImageView btnBack;

    // --- KHAI BÁO FIRESTORE ---
    private FirebaseFirestore db;
    private CollectionReference khachHangRef;

    private KhachHang khachHangToDelete = null;

    // --- HÀM TẠO MÃ KHÁCH HÀNG TỰ ĐỘNG ---
    // Logic: Lấy 6 số cuối của thời gian hiện tại để tạo mã ngắn gọn
    private String taoMaKhachHangTuDong() {
        long timestamp = System.currentTimeMillis();
        long shortId = timestamp % 1000000;
        return "KH" + shortId; // Ví dụ kết quả: KH832910
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        khachHangRef = db.collection("khachhang");

        // 2. LẮNG NGHE YÊU CẦU THÊM MỚI (Từ TaoHoSoKhachHangFragment)
        getParentFragmentManager().setFragmentResultListener("add_customer_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String newName = result.getString("new_name");
                String newPhone = result.getString("new_phone");
                String newDob = result.getString("new_dob");
                String newEmail = result.getString("new_email");

                // --- LOGIC QUAN TRỌNG: TẠO MÃ RIÊNG ---
                String customId = taoMaKhachHangTuDong();

                // Tạo đối tượng với ID vừa sinh
                KhachHang newKH = new KhachHang(customId, newName, newPhone, newDob, newEmail, R.drawable.ic_launcher_background);

                // Dùng .document(customId).set(...) để ID của Document trùng với Mã KH
                khachHangRef.document(customId).set(newKH)
                        .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Thêm thành công Mã: " + customId, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // 3. LẮNG NGHE YÊU CẦU XÓA (Từ XoaKhachHangFragment)
        getParentFragmentManager().setFragmentResultListener("delete_customer_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean confirmed = result.getBoolean("confirm_delete");
                if (confirmed && khachHangToDelete != null) {
                    // Xóa trên Firestore
                    String idToDelete = khachHangToDelete.getId();
                    if (idToDelete != null) {
                        khachHangRef.document(idToDelete).delete()
                                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Đã xóa khách hàng!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                    khachHangToDelete = null;
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo tên layout đúng với file XML của bạn (ví dụ fragment_dskh.xml)
        View view = inflater.inflate(R.layout.fragment_dskh, container, false);

        // Ánh xạ View
        rvKhachHang = view.findViewById(R.id.rvKhachHang);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnBack = view.findViewById(R.id.btnBack);

        // Cấu hình RecyclerView
        listKhachHang = new ArrayList<>();
        adapter = new KhachHangAdapter(listKhachHang,
                // Sự kiện Click vào item -> Mở chi tiết
                this::moManHinhChiTiet,
                // Sự kiện Click nút Xóa -> Mở xác nhận xóa
                (khachHang, position) -> {
                    khachHangToDelete = khachHang;
                    moManHinhXoa(khachHang);
                }
        );

        rvKhachHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKhachHang.setAdapter(adapter);

        // Gọi hàm lấy dữ liệu Realtime
        getDataFromFirestore();

        // Sự kiện các nút bấm
        btnAdd.setOnClickListener(v -> replaceFragment(new TaoHoSoKhachHangFragment()));

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        return view;
    }

    // Hàm lắng nghe thay đổi từ Firestore (Realtime)
    private void getDataFromFirestore() {
        khachHangRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", "Lỗi lắng nghe dữ liệu.", error);
                    return;
                }

                if (value != null) {
                    listKhachHang.clear();
                    for (DocumentSnapshot doc : value) {
                        KhachHang kh = doc.toObject(KhachHang.class);
                        if (kh != null) {
                            // Gán ID của document vào object để sau này dùng cho việc Xóa/Sửa
                            kh.setId(doc.getId());
                            listKhachHang.add(kh);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Hàm chuyển Fragment
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_frame, fragment); // ID này phải trùng với FrameLayout trong Activity chính
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Mở màn hình Chi Tiết
    private void moManHinhChiTiet(KhachHang kh) {
        ChiTietKhachHangFragment fragmentChiTiet = new ChiTietKhachHangFragment();
        Bundle bundle = new Bundle();

        // Truyền dữ liệu sang màn hình chi tiết
        bundle.putString("id", kh.getId()); // Đây chính là Mã KH (VD: KH829103)
        bundle.putString("name", kh.getTen());
        bundle.putString("phone", kh.getSdt());
        bundle.putString("dob", kh.getNgaySinh());
        bundle.putInt("avatar", kh.getAvatarResId());
        bundle.putString("email", kh.getEmail());

        fragmentChiTiet.setArguments(bundle);
        replaceFragment(fragmentChiTiet);
    }

    // Mở màn hình Xóa
    private void moManHinhXoa(KhachHang kh) {
        XoaKhachHangFragment fragmentXoa = new XoaKhachHangFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", kh.getTen());
        fragmentXoa.setArguments(bundle);
        replaceFragment(fragmentXoa);
    }
}