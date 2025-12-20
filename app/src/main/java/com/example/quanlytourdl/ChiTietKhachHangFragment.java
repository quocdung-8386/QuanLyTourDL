package com.example.quanlytourdl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChiTietKhachHangFragment extends Fragment {

    // Views cơ bản
    private ImageView btnBack, btnCopyCode, imgAvatar;
    private TextView tvCode, btnViewAllHistory;
    private EditText edtName, edtDob, edtGender, edtCitizenId, edtPhone, edtEmail, edtAddress;
    private MaterialButton btnEditProfile;
    private LinearLayout btnActionCall, btnActionMessage, btnActionEmail;

    // RecyclerView Lịch sử Tour
    private RecyclerView rvTourHistory;
    private List<Map<String, Object>> tourHistoryList;
    private TourHistoryAdapter historyAdapter;

    private String fullCustomerId;
    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_khach_hang, container, false);
        initViews(view);
        loadData();
        setupEvents();
        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvCode = view.findViewById(R.id.tvCode);
        btnCopyCode = view.findViewById(R.id.btnCopyCode);
        edtName = view.findViewById(R.id.edtName);
        edtDob = view.findViewById(R.id.edtDob);
        edtGender = view.findViewById(R.id.edtGender);
        edtCitizenId = view.findViewById(R.id.edtCitizenId);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtAddress = view.findViewById(R.id.edtAddress);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnActionCall = view.findViewById(R.id.btnActionCall);
        btnActionMessage = view.findViewById(R.id.btnActionMessage);
        btnActionEmail = view.findViewById(R.id.btnActionEmail);
        btnViewAllHistory = view.findViewById(R.id.btnViewAllHistory);

        // Khởi tạo RecyclerView Lịch sử
        rvTourHistory = view.findViewById(R.id.rvTourHistory);
        rvTourHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        tourHistoryList = new ArrayList<>();
    }

    private void loadData() {
        Bundle args = getArguments();
        if (args != null) {
            String rawId = args.getString("id");
            fullCustomerId = (rawId != null) ? rawId.replaceAll("\\s+", "") : "";

            // Hiển thị mã KH rút gọn
            String displayId = fullCustomerId.length() > 8 ? fullCustomerId.substring(0, 8) : fullCustomerId;
            tvCode.setText("#" + displayId.toUpperCase());

            edtName.setText(args.getString("name", ""));
            edtDob.setText(args.getString("dob", ""));
            edtGender.setText(args.getString("gender", ""));
            edtCitizenId.setText(args.getString("cccd", ""));
            edtPhone.setText(args.getString("phone", ""));
            edtEmail.setText(args.getString("email", ""));
            edtAddress.setText(args.getString("address", ""));

            // Avatar theo giới tính
            String gender = args.getString("gender", "");
            if ("Nam".equalsIgnoreCase(gender)) imgAvatar.setImageResource(R.drawable.ic_avatar_male);
            else if ("Nữ".equalsIgnoreCase(gender)) imgAvatar.setImageResource(R.drawable.ic_avatar_female);

            // TẢI LỊCH SỬ TỪ FIREBASE
            fetchTourHistoryFromFirebase(fullCustomerId);
        }
    }

    private void fetchTourHistoryFromFirebase(String customerId) {
        if (customerId.isEmpty()) return;

        FirebaseFirestore.getInstance().collection("dattour")
                .whereEqualTo("maKhachHang", customerId)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tourHistoryList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            data.put("idDatTour", doc.getId());
                            tourHistoryList.add(data);
                        }
                    }
                    historyAdapter = new TourHistoryAdapter(tourHistoryList, this::openTourDetail);
                    rvTourHistory.setAdapter(historyAdapter);
                })
                .addOnFailureListener(e -> Log.e("FirebaseHistory", "Lỗi: " + e.getMessage()));
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnCopyCode.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ID Khách hàng", fullCustomerId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Đã sao chép mã KH", Toast.LENGTH_SHORT).show();
        });

        btnEditProfile.setOnClickListener(v -> {
            if (!isEditing) toggleEditMode(true);
            else saveDataToFirebase();
        });

        btnActionCall.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            if(!phone.isEmpty()){
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        });
// Trong setupEvents()
        btnActionMessage.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            if(!phone.isEmpty()){
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + phone));
                startActivity(intent);
            }
        });

        btnActionEmail.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if(!email.isEmpty()){
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                startActivity(intent);
            }
        });
        btnViewAllHistory.setOnClickListener(v -> {
            LichSuTourFragment fragment = new LichSuTourFragment();
            Bundle bundle = new Bundle();
            bundle.putString("customerId", fullCustomerId);
            fragment.setArguments(bundle);


            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;
        EditText[] fields = {edtName, edtDob, edtGender, edtCitizenId, edtPhone, edtEmail, edtAddress};
        for (EditText f : fields) {
            f.setEnabled(enable);
            f.setBackgroundColor(enable ? Color.parseColor("#E3F2FD") : Color.TRANSPARENT);
        }
        btnEditProfile.setText(enable ? "Lưu thay đổi" : "Chỉnh sửa hồ sơ");
        btnEditProfile.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), enable ? R.color.green_700 : R.color.blue_700));
    }

    private void saveDataToFirebase() {
        Map<String, Object> update = new HashMap<>();
        update.put("ten", edtName.getText().toString().trim());
        update.put("sdt", edtPhone.getText().toString().trim());
        update.put("email", edtEmail.getText().toString().trim());
        update.put("diaChi", edtAddress.getText().toString().trim());

        FirebaseFirestore.getInstance().collection("khachhang").document(fullCustomerId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    toggleEditMode(false);
                });
    }

    private void openTourDetail(Map<String, Object> tour) {
        Toast.makeText(getContext(), "Xem chi tiết: " + tour.get("tenTour"), Toast.LENGTH_SHORT).show();
    }

    // --- INNER ADAPTER CẬP NHẬT THEO LAYOUT MỚI ---
    private class TourHistoryAdapter extends RecyclerView.Adapter<TourHistoryAdapter.ViewHolder> {
        private List<Map<String, Object>> list;
        private OnItemClickListener listener;

        public TourHistoryAdapter(List<Map<String, Object>> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = list.get(position);

            // Đổ dữ liệu theo ID mới trong XML của bạn
            holder.tvTourName.setText(String.valueOf(item.get("tenTour")));
            holder.tvDateRange.setText(String.valueOf(item.get("thoiGian")));
            holder.tvTourCode.setText("Mã tour: " + item.get("maTour"));
            holder.tvGuestCount.setText("Số khách: " + item.get("soLuongKhach"));

            String status = String.valueOf(item.get("trangThai"));
            holder.tvStatus.setText(status);

            // Logic đổi màu CardStatus và TextStatus
            if ("Hoàn thành".equalsIgnoreCase(status)) {
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else if ("Đã hủy".equalsIgnoreCase(status)) {
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
            } else {
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                holder.tvStatus.setTextColor(Color.parseColor("#1565C0"));
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTourName, tvDateRange, tvTourCode, tvGuestCount, tvStatus;
            CardView cardStatus;
            public ViewHolder(View v) {
                super(v);
                tvTourName = v.findViewById(R.id.tvTourName);
                tvDateRange = v.findViewById(R.id.tvDateRange);
                tvTourCode = v.findViewById(R.id.tvTourCode);
                tvGuestCount = v.findViewById(R.id.tvGuestCount);
                tvStatus = v.findViewById(R.id.tvStatus);
                cardStatus = v.findViewById(R.id.cardStatus);
            }
        }
    }

    interface OnItemClickListener { void onItemClick(Map<String, Object> data); }
}