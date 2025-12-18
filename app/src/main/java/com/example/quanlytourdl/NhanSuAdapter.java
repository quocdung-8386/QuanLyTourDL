package com.example.quanlytourdl;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NhanSuAdapter extends RecyclerView.Adapter<NhanSuAdapter.NhanSuViewHolder> {

    private List<NhanVien> mListNhanVien;

    public NhanSuAdapter(List<NhanVien> mListNhanVien) {
        this.mListNhanVien = mListNhanVien;
    }

    // Hàm cập nhật danh sách khi tìm kiếm
    public void setFilteredList(List<NhanVien> filteredList) {
        this.mListNhanVien = filteredList;
        notifyDataSetChanged();
    }

    // --- HÀM HỖ TRỢ LẤY ACTIVITY AN TOÀN (FIX LỖI CRASH) ---
    private AppCompatActivity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof AppCompatActivity) {
                return (AppCompatActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    @NonNull
    @Override
    public NhanSuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nhan_su, parent, false);
        return new NhanSuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NhanSuViewHolder holder, int position) {
        NhanVien nhanVien = mListNhanVien.get(position);
        if (nhanVien == null) return;

        // --- ĐÃ SỬA LỖI TẠI ĐÂY ---
        // Sử dụng phương thức getter chuẩn getFullName()
        holder.tvName.setText(nhanVien.getFullName());

        // Hiển thị ID
        holder.tvId.setText("ID: " + nhanVien.getId());

        if (nhanVien.getDepartment() != null && !nhanVien.getDepartment().isEmpty()) {
            holder.tvDepartment.setText("Phòng ban: " + nhanVien.getDepartment());
            holder.ivEdit.setVisibility(View.VISIBLE);
            if (holder.ivAddDepartment != null) holder.ivAddDepartment.setVisibility(View.GONE);
        } else {
            holder.tvDepartment.setText("Chưa có phòng ban");
            holder.ivEdit.setVisibility(View.GONE);
            if (holder.ivAddDepartment != null) holder.ivAddDepartment.setVisibility(View.VISIBLE);
        }

        // --- XỬ LÝ SỰ KIỆN CLICK ---
        View.OnClickListener listener = v -> {
            // 1. Đóng gói dữ liệu để gửi sang trang Sửa
            Bundle bundle = new Bundle();
            bundle.putSerializable("nhanvien_data", nhanVien);

            // Lưu ý: Đảm bảo bạn đã có TaoTaiKhoanNhanVienFragment
            TaoTaiKhoanNhanVienFragment fragment = new TaoTaiKhoanNhanVienFragment();
            fragment.setArguments(bundle);

            // 2. Lấy Activity an toàn
            AppCompatActivity activity = getActivityFromView(v);

            if (activity != null) {
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content_frame, fragment) // ID này phải khớp với container trong MainActivity của bạn
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.e("NhanSuAdapter", "Không tìm thấy AppCompatActivity từ View này!");
            }
        };

        // Gán sự kiện click cho các thành phần
        holder.itemView.setOnClickListener(listener);
        holder.ivEdit.setOnClickListener(listener);
        if (holder.btnProfile != null) {
            holder.btnProfile.setOnClickListener(listener);
        }
    }

    @Override
    public int getItemCount() {
        return mListNhanVien != null ? mListNhanVien.size() : 0;
    }

    public static class NhanSuViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvId, tvDepartment;
        ImageView ivEdit, ivAddDepartment;
        Button btnProfile;

        public NhanSuViewHolder(@NonNull View itemView) {
            super(itemView);
            // Các ID này phải khớp với file item_nhan_su.xml
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvId = itemView.findViewById(R.id.tv_id);
            tvDepartment = itemView.findViewById(R.id.tv_department);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivAddDepartment = itemView.findViewById(R.id.iv_add_department);
            btnProfile = itemView.findViewById(R.id.btn_profile);
        }
    }
}