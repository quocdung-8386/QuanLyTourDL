package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu; // Thêm thư viện PopupMenu
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

import java.text.Collator; // Thêm thư viện xử lý tiếng Việt
import java.util.ArrayList;
import java.util.Collections; // Thêm thư viện sắp xếp
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

        // LẮNG NGHE XÓA (Giữ nguyên logic cũ)
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

        // --- CẬP NHẬT: Thêm logic hiển thị Menu Lọc ---
        btnFilter.setOnClickListener(v -> showSortMenu());

        return view;
    }

    // --- MỚI: Hàm hiển thị Menu chọn A-Z ---
    private void showSortMenu() {
        PopupMenu popup = new PopupMenu(getContext(), btnFilter);
        // Thêm item: ID Group, ID Item, Order, Title
        popup.getMenu().add(0, 1, 0, "Sắp xếp tên: A -> Z");
        popup.getMenu().add(0, 2, 1, "Sắp xếp tên: Z -> A");

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        sortData(true); // A->Z
                        return true;
                    case 2:
                        sortData(false); // Z->A
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    // --- MỚI: Hàm xử lý logic sắp xếp tiếng Việt ---
    private void sortData(boolean ascending) {
        // Collator hỗ trợ so sánh dấu tiếng Việt chuẩn (a, ă, â...)
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));

        Collections.sort(listKhachHang, new Comparator<KhachHang>() {
            @Override
            public int compare(KhachHang o1, KhachHang o2) {
                String name1 = o1.getTen() != null ? o1.getTen() : "";
                String name2 = o2.getTen() != null ? o2.getTen() : "";

                if (ascending) {
                    return collator.compare(name1, name2); // A -> Z
                } else {
                    return collator.compare(name2, name1); // Z -> A
                }
            }
        });

        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), ascending ? "Đã xếp A-Z" : "Đã xếp Z-A", Toast.LENGTH_SHORT).show();
    }

    // --- GIỮ NGUYÊN CODE CŨ BÊN DƯỚI ---

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

        bundle.putString("id", kh.getId());
        bundle.putString("name", kh.getTen());
        bundle.putString("phone", kh.getSdt());
        bundle.putString("dob", kh.getNgaySinh());
        bundle.putString("email", kh.getEmail());
        bundle.putString("address", kh.getDiaChi());
        bundle.putString("gender", kh.getGioiTinh());
        bundle.putString("cccd", kh.getCccd());
        bundle.putString("nationality", kh.getQuocTich());

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