package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.NhaCungCap;

import java.util.List;

public class NhaCungCapAdapter extends RecyclerView.Adapter<NhaCungCapAdapter.ViewHolder> {

    private final Context context;
    private final List<NhaCungCap> nhaCungCapList;
    private final OnItemActionListener actionListener;

    public interface OnItemActionListener {
        void onEditClick(NhaCungCap ncc);
        void onViewClick(NhaCungCap ncc);
        void onDeleteClick(NhaCungCap ncc);
        void onTerminateContract(NhaCungCap ncc);
    }

    public NhaCungCapAdapter(Context context, List<NhaCungCap> list, OnItemActionListener listener) {
        this.context = context;
        this.nhaCungCapList = list;
        this.actionListener = listener;
    }

    // Tối ưu danh sách bằng DiffUtil (Chống lag khi cập nhật)
    public void updateList(List<NhaCungCap> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return nhaCungCapList.size(); }
            @Override
            public int getNewListSize() { return newList.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return nhaCungCapList.get(oldPos).getMaNhaCungCap().equals(newList.get(newPos).getMaNhaCungCap());
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return nhaCungCapList.get(oldPos).equals(newList.get(newPos));
            }
        });
        nhaCungCapList.clear();
        nhaCungCapList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
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

        holder.textProviderName.setText(ncc.getTenNhaCungCap());
        holder.textProviderId.setText("ID: " + ncc.getMaNhaCungCap());
        holder.textPhoneNumber.setText(ncc.getSoDienThoai() != null ? ncc.getSoDienThoai() : "N/A");
        holder.textStatus.setText("Hoạt động");

        // Gán sự kiện click
        holder.iconView.setOnClickListener(v -> actionListener.onViewClick(ncc));
        holder.iconEdit.setOnClickListener(v -> actionListener.onEditClick(ncc));
        holder.iconDelete.setOnClickListener(v -> actionListener.onDeleteClick(ncc));
    }

    @Override
    public int getItemCount() {
        return nhaCungCapList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textProviderName, textStatus, textProviderId, textPhoneNumber;
        ImageView iconEdit, iconView, iconDelete;

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