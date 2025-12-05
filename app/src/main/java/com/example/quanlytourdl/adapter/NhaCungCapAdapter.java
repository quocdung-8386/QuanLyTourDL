package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.NhaCungCap;
// Đã xóa imports FirebaseFirestore và DocumentReference vì logic xóa đã chuyển sang Fragment
// import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class NhaCungCapAdapter extends RecyclerView.Adapter<NhaCungCapAdapter.ViewHolder> {

    private static final String TAG = "NhaCungCapAdapter";
    private final Context context;
    private final List<NhaCungCap> nhaCungCapList;
    // Đã xóa db object

    // ***************************************************************
    // THAY ĐỔI 1: KHAI BÁO INTERFACE (THÊM onDeleteClick)
    // ***************************************************************
    public interface OnItemActionListener {
        void onEditClick(NhaCungCap nhaCungCap);
        void onViewClick(NhaCungCap nhaCungCap);
        void onDeleteClick(NhaCungCap nhaCungCap); // THÊM HÀM XỬ LÝ XÓA
    }

    private final OnItemActionListener actionListener;

    // ***************************************************************
    // THAY ĐỔI 2: CẬP NHẬT CONSTRUCTOR ĐỂ NHẬN LISTENER
    // ***************************************************************
    public NhaCungCapAdapter(Context context, List<NhaCungCap> nhaCungCapList, OnItemActionListener actionListener) {
        this.context = context;
        this.nhaCungCapList = nhaCungCapList;
        // Đã xóa this.db = FirebaseFirestore.getInstance();
        this.actionListener = actionListener; // Gán listener
    }

    // Constructor cũ (giữ lại nếu cần, nhưng nên dùng constructor mới)
    public NhaCungCapAdapter(Context context, List<NhaCungCap> nhaCungCapList) {
        this(context, nhaCungCapList, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_provider_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NhaCungCap ncc = nhaCungCapList.get(position);

        // 1. Ánh xạ dữ liệu
        holder.textProviderName.setText(ncc.getTenNhaCungCap());
        holder.textProviderId.setText("ID: " + ncc.getMaNhaCungCap());
        holder.textPhoneNumber.setText(ncc.getSoDienThoai());

        // 2. Cài đặt trạng thái
        holder.textStatus.setText("Hoạt động");

        // 3. Xử lý sự kiện click

        // Nút Xem chi tiết
        holder.iconView.setOnClickListener(v -> {
            // ***************************************************************
            // GỌI CALLBACK VIEW
            // ***************************************************************
            if (actionListener != null) {
                actionListener.onViewClick(ncc);
            } else {
                Toast.makeText(context, "Xem chi tiết: " + ncc.getTenNhaCungCap(), Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Sửa
        holder.iconEdit.setOnClickListener(v -> {
            // ***************************************************************
            // GỌI CALLBACK EDIT (QUAN TRỌNG NHẤT)
            // ***************************************************************
            if (actionListener != null) {
                actionListener.onEditClick(ncc);
            } else {
                Toast.makeText(context, "Sửa: " + ncc.getTenNhaCungCap() + " (Listener is null)", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Xóa
        holder.iconDelete.setOnClickListener(v -> {
            // ***************************************************************
            // THAY ĐỔI 3: GỌI CALLBACK DELETE THAY VÌ XÓA TRỰC TIẾP
            // ***************************************************************
            if (actionListener != null) {
                actionListener.onDeleteClick(ncc);
            } else {
                Toast.makeText(context, "Không thể xóa: Listener is null", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return nhaCungCapList.size();
    }

    // --- LOGIC XÓA FIRESTORE ---
    // ***************************************************************
    // THAY ĐỔI 4: XÓA HÀM deleteSupplierFromFirestore() KHỎI ADAPTER
    // Hàm này đã được chuyển sang KinhDoanhFragment
    // ***************************************************************


    // --- VIEWHOLDER (Giữ nguyên) ---
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textProviderName;
        TextView textStatus;
        TextView textProviderId;
        TextView textPhoneNumber;
        ImageView iconEdit;
        ImageView iconView;
        ImageView iconDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textProviderName = itemView.findViewById(R.id.text_provider_name);
            textStatus = itemView.findViewById(R.id.text_status);
            textProviderId = itemView.findViewById(R.id.text_provider_id);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
            iconEdit = itemView.findViewById(R.id.icon_edit);
            iconView = itemView.findViewById(R.id.icon_view);
            iconDelete = itemView.findViewById(R.id.icon_delete);
        }
    }
}