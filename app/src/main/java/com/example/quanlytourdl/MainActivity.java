package com.example.quanlytourdl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.DashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

// Ghi chú: Tôi đã đổi package name từ com.example.quanlytourdl sang com.example.apponline
// theo file build.gradle.kts bạn đã cung cấp gần đây nhất.

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
                    // TODO: Tạo Fragment KinhDoanhFragment
                    Toast.makeText(MainActivity.this, "Kinh doanh", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_cskh) {
                    // TODO: Tạo Fragment CSKHFragment
                    Toast.makeText(MainActivity.this, "Chăm sóc khách hàng", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_canhan) {
                    // TODO: Tạo Fragment CaNhanFragment
                    Toast.makeText(MainActivity.this, "Cá nhân", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        // Tải Fragment Thống kê (Dashboard) khi khởi động lần đầu
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_thongke);
            loadFragment(new DashboardFragment());
        }
    }

    /**
     * Hàm thay thế Fragment trong FrameLayout
     * @param fragment Fragment cần hiển thị
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // frame_container là ID của FrameLayout trong activity_main.xml
        transaction.replace(R.id.main_content_frame, fragment);
        transaction.commit();
    }
}