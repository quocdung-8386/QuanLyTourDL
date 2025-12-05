package com.example.quanlytourdl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

// Đảm bảo bạn đã tạo và import tất cả các Fragment này
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.DashboardFragment;
// THÊM CÁC IMPORT CỦA FRAGMENT CHƯA CÓ (CẦN TẠO CÁC FILE NÀY TRONG DỰ ÁN CỦA BẠN)
import com.example.quanlytourdl.KinhDoanhFragment;
import com.example.quanlytourdl.CSKHFragment;
import com.example.quanlytourdl.CaNhanFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Thiết lập listener cho Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.nav_thongke) {
                    loadFragment(new DashboardFragment());
                    return true;
                } else if (itemId == R.id.nav_kinhdoanh) {
                    // SỬA LỖI: Thay thế Context sai bằng MainActivity.this và tải Fragment
                    loadFragment(new KinhDoanhFragment());
                    Toast.makeText(MainActivity.this, "Kinh doanh", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_cskh) {
                    // SỬA LỖI: Thay thế Context sai bằng MainActivity.this và tải Fragment
                    loadFragment(new CSKHFragment());
                    Toast.makeText(MainActivity.this, "Chăm sóc khách hàng", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_canhan) {
                    // SỬA LỖI: Thay thế Context sai bằng MainActivity.this và tải Fragment
                    loadFragment(new CaNhanFragment());
                    Toast.makeText(MainActivity.this, "Cá nhân", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        // Tải Fragment Thống kê (Dashboard) khi khởi động lần đầu
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_thongke);
            // Hàm loadFragment đã được gọi trong setOnItemSelectedListener,
            // nhưng để đảm bảo, bạn vẫn có thể gọi lại ở đây hoặc loại bỏ dòng này.
            // loadFragment(new DashboardFragment());
        }
    }

    /**
     * Hàm thay thế Fragment trong FrameLayout
     * @param fragment Fragment cần hiển thị
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // R.id.main_content_frame là ID của FrameLayout trong activity_main.xml (giả định)
        transaction.replace(R.id.main_content_frame, fragment);
        transaction.commit();
    }
}