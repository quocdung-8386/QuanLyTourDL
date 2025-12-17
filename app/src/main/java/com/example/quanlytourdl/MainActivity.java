package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

// Fragment
import com.example.quanlytourdl.DashboardFragment;
import com.example.quanlytourdl.KinhDoanhFragment;
import com.example.quanlytourdl.CSKHFragment;
import com.example.quanlytourdl.CaNhanFragment;

// Firebase
import com.example.quanlytourdl.firebase.FBHelper;
import com.example.quanlytourdl.model.UserModel;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FBHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase Helper
        fbHelper = new FBHelper(this);

        // Ánh xạ BottomNavigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Xử lý click Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(
                            @NonNull MenuItem item
                    ) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.nav_thongke) {
                            loadFragment(new DashboardFragment());
                            return true;
                        }

                        if (itemId == R.id.nav_kinhdoanh) {
                            loadFragment(new KinhDoanhFragment());
                            return true;
                        }

                        if (itemId == R.id.nav_cskh) {
                            loadFragment(new CSKHFragment());
                            return true;
                        }

                        if (itemId == R.id.nav_canhan) {
                            loadFragment(new CaNhanFragment());
                            return true;
                        }

                        return false;
                    }
                }
        );

        // Fragment mặc định khi mở app
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_kinhdoanh);
        }

        // Kiểm tra phân quyền
        checkUserRoleAndSetupMenu();
    }

    // ================= PHÂN QUYỀN MENU =================
    private void checkUserRoleAndSetupMenu() {
        Menu menu = bottomNavigationView.getMenu();
        MenuItem itemThongKe = menu.findItem(R.id.nav_thongke);

        // Mặc định ẩn Thống kê
        itemThongKe.setVisible(false);

        fbHelper.getCurrentUserData(new FBHelper.DataListener() {
            @Override
            public void onDataReceived(UserModel user) {
                if (user == null) return;

                String role = user.getRole();

                if ("Quản lý".equals(role)) {
                    itemThongKe.setVisible(true);
                    Toast.makeText(
                            MainActivity.this,
                            "Xin chào Quản lý: " + user.getFullName(),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    itemThongKe.setVisible(false);
                }
            }

            @Override
            public void onError(String error) {
                // Lỗi mạng hoặc chưa đăng nhập
                itemThongKe.setVisible(false);
            }
        });
    }

    // ================= LOAD FRAGMENT =================
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    // R.id.main_content_frame
                    .replace(R.id.main_content_frame, fragment)
                    .commit();
        }
    }
}