package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.CongNoModel;

import java.text.DecimalFormat;
import java.util.List;

public class CongNoAdapter extends RecyclerView.Adapter<CongNoAdapter.ViewHolder> {

    private List<CongNoModel> mList;
    private Context context;
    private OnItemClickListener listener; // Thêm listener

    // Interface để Fragment lắng nghe sự kiện click
    public interface OnItemClickListener {
        void onItemClick(CongNoModel model);
    }

    // Cập nhật Constructor để nhận listener
    public CongNoAdapter(List<CongNoModel> mList, Context context, OnItemClickListener listener) {
        this.mList = mList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cong_no_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CongNoModel item = mList.get(position);
        if (item == null) return;

        // 1. Gán dữ liệu cơ bản
        holder.tvName.setText(item.getTenNcc());
        holder.tvMaHD.setText(item.getMaHopDong());
        holder.tvNoiDung.setText(item.getNoiDung());
        holder.tvHan.setText("Hạn " + item.getNgayHan());

        // 2. Định dạng tiền tệ
        DecimalFormat df = new DecimalFormat("#,###đ");
        holder.tvTien.setText(df.format(item.getSoTien()));

        // 3. Xử lý Icon theo loại dịch vụ
        updateServiceIcon(holder.imgService, item.getLoaiDichVu());

        // 4. Xử lý trạng thái (Màu sắc & Tag)
        updateStatusUI(holder, item.getTrangThai());

        // 5. Xử lý sự kiện click vào toàn bộ Card để mở PheDuyetCongNoDialogFragment
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        // 6. Nút Ghi nhận (Nếu cần xử lý riêng)
        holder.btnGhiNhan.setOnClickListener(v -> {
            Toast.makeText(context, "Mở chi tiết phê duyệt cho: " + item.getMaHopDong(), Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onItemClick(item);
        });
    }

    private void updateServiceIcon(ImageView img, String type) {
        if (type == null) {
            img.setImageResource(R.drawable.ic_service_default);
            return;
        }
        String lowerType = type.toLowerCase();
        if (lowerType.contains("xe")) img.setImageResource(R.drawable.ic_bus);
        else if (lowerType.contains("khách sạn")) img.setImageResource(R.drawable.ic_hotel);
        else if (lowerType.contains("ăn uống") || lowerType.contains("nhà hàng")) img.setImageResource(R.drawable.ic_restaurant);
        else img.setImageResource(R.drawable.ic_service_default);
    }

    private void updateStatusUI(ViewHolder holder, String status) {
        if ("Quá hạn".equalsIgnoreCase(status)) {
            holder.viewLine.setBackgroundColor(Color.parseColor("#E53E3E"));
            holder.tvStatus.setText("Quá hạn");
            holder.tvStatus.setTextColor(Color.parseColor("#E53E3E"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_red);
            holder.tvHan.setTextColor(Color.parseColor("#E53E3E"));
        } else if ("Đã phê duyệt".equalsIgnoreCase(status)) {
            holder.viewLine.setBackgroundColor(Color.parseColor("#38A169"));
            holder.tvStatus.setText("Đã duyệt");
            holder.tvStatus.setTextColor(Color.parseColor("#38A169"));
            holder.tvStatus.setBackgroundResource(R.color.green_700);
            holder.tvHan.setTextColor(Color.parseColor("#718096"));
        } else {
            holder.viewLine.setBackgroundColor(Color.parseColor("#ECC94B"));
            holder.tvStatus.setText("Chờ TT");
            holder.tvStatus.setTextColor(Color.parseColor("#D69E2E"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_yellow);
            holder.tvHan.setTextColor(Color.parseColor("#718096"));
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMaHD, tvNoiDung, tvHan, tvTien, tvStatus;
        ImageView imgService;
        AppCompatButton btnGhiNhan;
        View viewLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNhaCungCapName);
            tvMaHD = itemView.findViewById(R.id.tvMaHopDong);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDungTour);
            tvHan = itemView.findViewById(R.id.tvHanThanhToan);
            tvTien = itemView.findViewById(R.id.tvSoTienCongNo);
            tvStatus = itemView.findViewById(R.id.tvStatusTag);
            imgService = itemView.findViewById(R.id.imgServiceType);
            btnGhiNhan = itemView.findViewById(R.id.btnGhiNhanTT);
            viewLine = itemView.findViewById(R.id.viewStatusLine);
        }
    }
}