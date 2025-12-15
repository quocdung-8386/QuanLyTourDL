package com.example.quanlytourdl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmployeePermissionAdapter extends RecyclerView.Adapter<EmployeePermissionAdapter.EmployeePermissionViewHolder> {

    private List<EmployeePermission> mEmployeePermissions;
    private Context mContext;

    public EmployeePermissionAdapter(Context context, List<EmployeePermission> employeePermissions) {
        mContext = context;
        mEmployeePermissions = employeePermissions;
    }

    @NonNull
    @Override
    public EmployeePermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_employee_permission, parent, false);
        return new EmployeePermissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeePermissionViewHolder holder, int position) {
        EmployeePermission employeePermission = mEmployeePermissions.get(position);
        holder.tvEmployeeName.setText(employeePermission.getEmployeeName());
        holder.tvEmployeeRole.setText("Vai trò: " + employeePermission.getEmployeeRole());

        holder.llPermissionsContainer.removeAllViews();

        for (Permission permission : employeePermission.getPermissions()) {
            View permissionView = LayoutInflater.from(mContext).inflate(R.layout.item_permission, holder.llPermissionsContainer, false);

            TextView tvPermissionName = permissionView.findViewById(R.id.tv_permission_name);
            TextView tvPermissionDescription = permissionView.findViewById(R.id.tv_permission_description);
            SwitchCompat switchPermission = permissionView.findViewById(R.id.switch_permission);

            tvPermissionName.setText(permission.getName());
            tvPermissionDescription.setText(permission.getDescription());
            switchPermission.setChecked(permission.isEnabled());

            holder.llPermissionsContainer.addView(permissionView);
        }
    }

    @Override
    public int getItemCount() {
        return mEmployeePermissions.size();
    }

    public static class EmployeePermissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName;
        TextView tvEmployeeRole;
        LinearLayout llPermissionsContainer;

        public EmployeePermissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tv_employee_name);
            tvEmployeeRole = itemView.findViewById(R.id.tv_employee_role);
            llPermissionsContainer = itemView.findViewById(R.id.ll_permissions_container);
        }
    }
}