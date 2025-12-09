package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class TwoFactorAuthActivity extends AppCompatActivity {

    private ImageView btnBack;
    private SwitchMaterial switchMaster;
    private LinearLayout layoutApp, layoutSms;
    private ImageView iconCheckApp, iconCheckSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_factor_auth);

        // 1. Ánh xạ View
        btnBack = findViewById(R.id.btn_back_2fa);
        switchMaster = findViewById(R.id.switch_master_2fa);
        layoutApp = findViewById(R.id.layout_option_app);
        layoutSms = findViewById(R.id.layout_option_sms);
        iconCheckApp = findViewById(R.id.icon_check_app);
        iconCheckSms = findViewById(R.id.icon_check_sms);

        // 2. Xử lý sự kiện nút Back
        btnBack.setOnClickListener(v -> finish());

        // 3. Xử lý Switch tổng
        switchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Đã BẬT" : "Đã TẮT";
            Toast.makeText(this, "Xác thực 2 yếu tố: " + status, Toast.LENGTH_SHORT).show();
            
            // Disable giao diện bên dưới nếu tắt Switch
            layoutApp.setEnabled(isChecked);
            layoutSms.setEnabled(isChecked);
            layoutApp.setAlpha(isChecked ? 1.0f : 0.5f);
            layoutSms.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        // 4. Logic chọn Phương thức (Radio Button logic)
        layoutApp.setOnClickListener(v -> updateSelection(true));
        layoutSms.setOnClickListener(v -> updateSelection(false));
    }

    private void updateSelection(boolean isAppSelected) {
        if (isAppSelected) {
            // Chọn App: Viền xanh, hiện tick
            layoutApp.setBackgroundResource(R.drawable.bg_border_green_rounded);
            iconCheckApp.setVisibility(View.VISIBLE);

            // SMS: Viền xám, ẩn tick
            layoutSms.setBackgroundResource(R.drawable.bg_border_gray_rounded);
            iconCheckSms.setVisibility(View.INVISIBLE);
        } else {
            // Ngược lại
            layoutApp.setBackgroundResource(R.drawable.bg_border_gray_rounded);
            iconCheckApp.setVisibility(View.INVISIBLE);

            layoutSms.setBackgroundResource(R.drawable.bg_border_green_rounded);
            iconCheckSms.setVisibility(View.VISIBLE);
        }
    }
}