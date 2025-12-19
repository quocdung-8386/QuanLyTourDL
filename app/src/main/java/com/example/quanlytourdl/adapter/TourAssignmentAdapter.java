package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TourAssignmentAdapter extends RecyclerView.Adapter<TourAssignmentAdapter.TourAssignmentViewHolder> {
    private final List<Tour> tourList;
    private final Context context;
    private final AssignmentListener listener;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    public interface AssignmentListener { void onAssignTourClick(Tour tour); }

    public TourAssignmentAdapter(Context context, List<Tour> tourList, AssignmentListener listener) {
        this.context = context;
        this.tourList = tourList; // Tham chiếu đến danh sách trong Fragment
        this.listener = listener;
    }

    public void updateList(List<Tour> newList) {
        // Tránh lỗi tham chiếu: Nếu cùng 1 list thì không clear()
        if (newList != this.tourList) {
            this.tourList.clear();
            if (newList != null) this.tourList.addAll(newList);
        }
        notifyDataSetChanged();
        Log.d("Adapter", "Thực tế hiển thị: " + getItemCount());
    }

    @NonNull
    @Override
    public TourAssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_tour_assignment, p, false);
        return new TourAssignmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TourAssignmentViewHolder h, int p) {
        Tour tour = tourList.get(p);
        h.tvCode.setText("Mã: " + tour.getMaTour());
        h.tvName.setText(tour.getTenTour());
        h.tvDate.setText("Ngày: " + (tour.getNgayKhoiHanh() != null ? df.format(tour.getNgayKhoiHanh()) : "N/A"));

        if ("DA_GAN_NHAN_VIEN".equals(tour.getStatus())) {
            h.tvStatus.setText("ĐÃ GÁN: " + tour.getAssignedGuideName() + " - " + tour.getAssignedVehicleLicensePlate());
            h.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green_700));
            h.btnAssign.setText("Xem/Sửa");
        } else {
            h.tvStatus.setText("CHƯA PHÂN CÔNG");
            h.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_700));
            h.btnAssign.setText("Gán Ngay");
        }
        h.btnAssign.setOnClickListener(v -> listener.onAssignTourClick(tour));
    }

    @Override
    public int getItemCount() { return tourList.size(); }

    static class TourAssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvDate, tvStatus;
        MaterialButton btnAssign;
        public TourAssignmentViewHolder(View v) {
            super(v);
            tvCode = v.findViewById(R.id.text_tour_code);
            tvName = v.findViewById(R.id.text_tour_name);
            tvDate = v.findViewById(R.id.text_departure_date);
            tvStatus = v.findViewById(R.id.text_assignment_status);
            btnAssign = v.findViewById(R.id.btn_assign_now);
        }
    }
}