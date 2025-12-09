package com.example.quanlytourdl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

// Import các Fragment
import com.example.quanlytourdl.DashboardFragment;
import com.example.quanlytourdl.KinhDoanhFragment;
import com.example.quanlytourdl.CSKHFragment;
import com.example.quanlytourdl.CaNhanFragment;

// Import Firebase Helper và Model (QUAN TRỌNG)
import com.example.quanlytourdl.firebase.FBHelper;
import com.example.quanlytourdl.model.UserModel;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FBHelper fbHelper; // 1. Khai báo Helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Khởi tạo FBHelper
        fbHelper = new FBHelper(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Thiết lập listener cho Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Dùng if-else if thay vì switch-case
                if (itemId == R.id.nav_thongke) {
                    loadFragment(new DashboardFragment());
                    return true;
                } else if (itemId == R.id.nav_kinhdoanh) {
                    loadFragment(new KinhDoanhFragment());
                    return true;
                } else if (itemId == R.id.nav_cskh) {
                    loadFragment(new CSKHFragment());
                    return true;
                } else if (itemId == R.id.nav_canhan) {
                    loadFragment(new CaNhanFragment());
                    return true;
                }
                return false;
            }
        });

        // 3. Tải Fragment mặc định khi mở App
        if (savedInstanceState == null) {
            // LƯU Ý QUAN TRỌNG: Mặc định ta load tab "Kinh doanh" trước.
            // Vì tab này ai cũng có quyền xem. Nếu load "Thống kê" ngay lập tức,
            // nhân viên sẽ nhìn thấy màn hình thống kê trước khi code kịp ẩn đi.
            bottomNavigationView.setSelectedItemId(R.id.nav_kinhdoanh);
        }

        // 4. Gọi hàm kiểm tra quyền để Ẩn/Hiện menu Thống kê
        checkUserRoleAndSetupMenu();
    }

    // Hàm xử lý phân quyền
    private void checkUserRoleAndSetupMenu() {
        Menu menu = bottomNavigationView.getMenu();
        // Lấy item Thống kê ra
        MenuItem itemThongKe = menu.findItem(R.id.nav_thongke);

        // Bước 1: Mặc định ẩn nút Thống kê đi để bảo mật
        itemThongKe.setVisible(false);

        // Bước 2: Hỏi Firebase xem người này là ai
        fbHelper.getCurrentUserData(new FBHelper.DataListener() {
            @Override
            public void onDataReceived(UserModel user) {
                if (user != null) {
                    String role = user.getRole();

                    // Bước 3: Nếu đúng là "Quản lý" thì mới cho hiện lại
                    if ("Quản lý".equals(role)) {
                        itemThongKe.setVisible(true);
                        // (Tùy chọn) Nếu là quản lý, có thể tự động chuyển sang tab Thống kê luôn
                        // bottomNavigationView.setSelectedItemId(R.id.nav_thongke);
                        Toast.makeText(MainActivity.this, "Xin chào Quản lý: " + user.getFullName(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Nếu là Nhân viên -> Giữ nguyên trạng thái ẩn
                        itemThongKe.setVisible(false);
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Lỗi mạng hoặc chưa đăng nhập -> Ẩn chức năng cao cấp
                itemThongKe.setVisible(false);
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_frame, fragment);
        transaction.commit();
    }
}