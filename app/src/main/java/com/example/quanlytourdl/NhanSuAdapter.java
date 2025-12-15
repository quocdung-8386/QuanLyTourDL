package com.example.quanlytourdl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NhanSuAdapter extends RecyclerView.Adapter<NhanSuAdapter.NhanSuViewHolder> {

    private List<NhanVien> mListNhanVien;

    public NhanSuAdapter(List<NhanVien> mListNhanVien) {
        this.mListNhanVien = mListNhanVien;
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
        if (nhanVien == null) {
            return;
        }
//        holder.ivAvatar.setImageResource(nhanVien.getAvatar());
        holder.tvName.setText(nhanVien.getName());
        holder.tvId.setText("ID: " + nhanVien.getId());
        if (nhanVien.getDepartment() != null && !nhanVien.getDepartment().isEmpty()) {
            holder.tvDepartment.setText("Phòng ban: " + nhanVien.getDepartment());
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivAddDepartment.setVisibility(View.GONE);
        } else {
            holder.tvDepartment.setText("Chưa có phòng ban");
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivAddDepartment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (mListNhanVien != null) {
            return mListNhanVien.size();
        }
        return 0;
    }

    public class NhanSuViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView ivAvatar;
        private TextView tvName;
        private TextView tvId;
        private TextView tvDepartment;
        private ImageView ivEdit;
        private ImageView ivAddDepartment;
        private Button btnProfile;

        public NhanSuViewHolder(@NonNull View itemView) {
            super(itemView);

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