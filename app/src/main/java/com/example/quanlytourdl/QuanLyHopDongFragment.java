package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import com.example.quanlytourdl.adapter.HopDongAdapter;
import com.example.quanlytourdl.model.HopDong;
import com.example.quanlytourdl.DetailFragment;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class QuanLyHopDongFragment extends Fragment implements HopDongAdapter.OnItemActionListener {

    private static final String TAG = "QuanLyHopDongFragment";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final long THIRTY_DAYS_IN_MILLIS = TimeUnit.DAYS.toMillis(30);

    private RecyclerView recyclerView;
    private HopDongAdapter adapter;
    private List<HopDong> fullHopDongList = new ArrayList<>();
    private List<HopDong> filteredHopDongList = new ArrayList<>();

    private FirebaseFirestore db;
    private CollectionReference hopDongRef;
    private ListenerRegistration listenerRegistration;

    private EditText etSearch;
    private Button btnFilterAll, btnFilterActive, btnFilterUpcoming, btnFilterExpired;
    private String currentFilter = "Tất cả";

    // Constants cho trạng thái
    private static final String TRANG_THAI_TAT_CA = "Tất cả";
    private static final String TRANG_THAI_DANG_HIEU_LUC = "Đang hiệu lực";
    private static final String TRANG_THAI_SAP_HET_HAN = "Sắp hết hạn";
    private static final String TRANG_THAI_DA_HET_HAN = "Đã hết hạn";
    private static final String TRANG_THAI_DA_CHAM_DUT_DB = "Đã Chấm dứt";
    private static final String TRANG_THAI_NCC_DA_XOA = "Nhà Cung Cấp Đã Xóa";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        hopDongRef = db.collection("HopDong");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quanlyhopdong, container, false);
        setupViews(view);
        setupRecyclerView(view);
        setupFilterButtons(view);
        loadHopDongData();
        return view;
    }

    private void setupViews(View view) {
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        etSearch = view.findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterDataList(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        View fabAdd = view.findViewById(R.id.fab_add_contract);
        if (fabAdd != null) fabAdd.setOnClickListener(v -> openFragment(new TaoHopDongFragment(), "Thêm HĐ"));
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_contracts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HopDongAdapter(requireContext(), filteredHopDongList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterButtons(View view) {
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterActive = view.findViewById(R.id.btn_filter_active);
        btnFilterUpcoming = view.findViewById(R.id.btn_filter_upcoming);
        btnFilterExpired = view.findViewById(R.id.btn_filter_expired);

        View.OnClickListener filterClick = v -> {
            resetStyles();
            Button b = (Button) v;
            b.setBackgroundResource(R.drawable.bg_filter_button_selected);
            b.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            currentFilter = b.getText().toString();
            filterDataList(etSearch.getText().toString().trim());
        };

        btnFilterAll.setOnClickListener(filterClick);
        btnFilterActive.setOnClickListener(filterClick);
        btnFilterUpcoming.setOnClickListener(filterClick);
        btnFilterExpired.setOnClickListener(filterClick);
    }

    private void resetStyles() {
        int bg = R.drawable.bg_filter_button;
        int color = ContextCompat.getColor(requireContext(), R.color.grey_text);
        for (Button b : new Button[]{btnFilterAll, btnFilterActive, btnFilterUpcoming, btnFilterExpired}) {
            b.setBackgroundResource(bg);
            b.setTextColor(color);
        }
    }

    private void loadHopDongData() {
        if (listenerRegistration != null) listenerRegistration.remove();

        listenerRegistration = hopDongRef.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            fullHopDongList.clear();
            Date today = getZeroTimeDate(new Date());
            long thirtyDaysFromNow = today.getTime() + THIRTY_DAYS_IN_MILLIS;

            for (QueryDocumentSnapshot doc : snapshots) {
                HopDong hd = doc.toObject(HopDong.class);
                hd.setDocumentId(doc.getId());

                if (TRANG_THAI_NCC_DA_XOA.equals(hd.getTrangThai())) continue;

                hd.setTrangThai(calculateStatus(hd, today, thirtyDaysFromNow));
                fullHopDongList.add(hd);
            }
            filterDataList(etSearch.getText().toString().trim());
        });
    }

    private String calculateStatus(HopDong hd, Date today, long limit) {
        if (TRANG_THAI_DA_CHAM_DUT_DB.equals(hd.getTrangThai())) return TRANG_THAI_DA_CHAM_DUT_DB;

        try {
            Date expiry = DATE_FORMAT.parse(hd.getNgayHetHan());
            if (expiry == null) return TRANG_THAI_DANG_HIEU_LUC;
            if (expiry.before(today)) return TRANG_THAI_DA_HET_HAN;
            if (expiry.getTime() <= limit) return TRANG_THAI_SAP_HET_HAN;
        } catch (Exception e) { return TRANG_THAI_DANG_HIEU_LUC; }

        return TRANG_THAI_DANG_HIEU_LUC;
    }

    private void filterDataList(String query) {
        String lowerQuery = query.toLowerCase(Locale.getDefault());

        List<HopDong> statusFiltered = fullHopDongList.stream()
                .filter(hd -> {
                    if (currentFilter.equals(TRANG_THAI_TAT_CA)) return true;
                    if (currentFilter.equals(TRANG_THAI_DA_HET_HAN)) {
                        return hd.getTrangThai().equals(TRANG_THAI_DA_HET_HAN) || hd.getTrangThai().equals(TRANG_THAI_DA_CHAM_DUT_DB);
                    }
                    return hd.getTrangThai().equals(currentFilter);
                }).collect(Collectors.toList());

        filteredHopDongList.clear();
        if (lowerQuery.isEmpty()) {
            filteredHopDongList.addAll(statusFiltered);
        } else {
            statusFiltered.stream()
                    .filter(hd -> (hd.getMaHopDong() != null && hd.getMaHopDong().toLowerCase().contains(lowerQuery)) ||
                            (hd.getNhaCungCap() != null && hd.getNhaCungCap().toLowerCase().contains(lowerQuery)))
                    .forEach(filteredHopDongList::add);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewClick(HopDong hopDong) {
        // ⭐ Sử dụng Factory Method mới của DetailFragment
        Fragment detailFragment = DetailFragment.newInstance((Serializable) hopDong);
        openFragment(detailFragment, "Chi tiết: " + hopDong.getMaHopDong());
    }

    @Override
    public void onEditClick(HopDong hopDong) {
        Bundle b = new Bundle();
        b.putString("contract_id", hopDong.getDocumentId());
        SuaHopDongFragment f = new SuaHopDongFragment();
        f.setArguments(b);
        openFragment(f, "Sửa HĐ");
    }

    @Override
    public void onDeleteClick(HopDong hopDong) {
        Bundle b = new Bundle();
        b.putString("supplier_id", hopDong.getSupplierId());
        b.putString("contract_id", hopDong.getDocumentId());
        ChamDutHopDongFragment f = new ChamDutHopDongFragment();
        f.setArguments(b);
        openFragment(f, "Chấm dứt HĐ");
    }

    private void openFragment(Fragment f, String log) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.main_content_frame, f)
                .addToBackStack(null)
                .commit();
        Log.d(TAG, log);
    }

    private Date getZeroTimeDate(Date date) {
        Calendar res = Calendar.getInstance();
        res.setTime(date);
        res.set(Calendar.HOUR_OF_DAY, 0);
        res.set(Calendar.MINUTE, 0);
        res.set(Calendar.SECOND, 0);
        res.set(Calendar.MILLISECOND, 0);
        return res.getTime();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}