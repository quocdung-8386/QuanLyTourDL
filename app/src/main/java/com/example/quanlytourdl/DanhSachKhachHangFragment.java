package com.example.quanlytourdl;

import android.os.Bundle;
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

import com.example.quanlytourdl.model.KhachHang;
import com.example.quanlytourdl.adapter.KhachHangAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DanhSachKhachHangFragment extends Fragment {

    private RecyclerView rvKhachHang;
    private KhachHangAdapter adapter;
    private List<KhachHang> listKhachHang;
    private ImageView btnAdd, btnBack;

    private DatabaseReference mDatabase;
    private KhachHang khachHangToDelete = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kết nối Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("khachhang");

        // 2. LẮNG NGHE YÊU CẦU THÊM MỚI (Đã update thêm Ngày Sinh)
        getParentFragmentManager().setFragmentResultListener("add_customer_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String newName = result.getString("new_name");
                String newPhone = result.getString("new_phone");
                String newDob = result.getString("new_dob"); // Nhận ngày sinh

                // Đẩy lên Firebase
                String id = mDatabase.push().getKey();
                if (id != null) {
                    // Tạo đối tượng với đầy đủ thông tin (bao gồm ngày sinh)
                    KhachHang newKH = new KhachHang(id, newName, newPhone, newDob, R.drawable.ic_launcher_background);

                    mDatabase.child(id).setValue(newKH, (error, ref) -> {
                        if (error == null) {
                            Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // 3. LẮNG NGHE YÊU CẦU XÓA
        getParentFragmentManager().setFragmentResultListener("delete_customer_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean confirmed = result.getBoolean("confirm_delete");
                if (confirmed && khachHangToDelete != null) {
                    String idToDelete = khachHangToDelete.getId();
                    if (idToDelete != null) {
                        mDatabase.child(idToDelete).removeValue((error, ref) -> {
                            if (error == null) {
                                Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    khachHangToDelete = null;
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dskh, container, false);

        rvKhachHang = view.findViewById(R.id.rvKhachHang);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnBack = view.findViewById(R.id.btnBack);

        listKhachHang = new ArrayList<>();
        adapter = new KhachHangAdapter(listKhachHang,
                this::moManHinhChiTiet, // Method reference cho gọn
                (khachHang, position) -> {
                    khachHangToDelete = khachHang;
                    moManHinhXoa(khachHang);
                }
        );

        rvKhachHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKhachHang.setAdapter(adapter);

        getDataFromFirebase();

        btnAdd.setOnClickListener(v -> replaceFragment(new TaoHoSoKhachHangFragment()));
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        return view;
    }

    private void getDataFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listKhachHang.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    KhachHang kh = dataSnapshot.getValue(KhachHang.class);
                    if (kh != null) {
                        listKhachHang.add(kh);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải data", Toast.LENGTH_SHORT).show();
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
        bundle.putString("name", kh.getTen());
        bundle.putString("phone", kh.getSdt());
        bundle.putString("dob", kh.getNgaySinh()); // Truyền thêm ngày sinh sang màn hình chi tiết
        bundle.putInt("avatar", kh.getAvatarResId());
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