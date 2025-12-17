package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

// Firebase Imports
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Charts Imports
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    // Views
    private TextView textMonthFilter;
    private TabLayout tabLayout;
    private NestedScrollView nestedScrollView;

    // Biểu đồ
    private LineChart lineChartRevenue;
    private BarChart barChartTours;
    private PieChart pieChartCustomers;
    private PieChart pieChartDebt;
    private BarChart barChartPerformance;

    // Containers
    private CardView cardRevenue;
    private CardView cardTours;
    private GridLayout gridCustomersDebt;
    private CardView cardPerformance;

    // Firebase
    private FirebaseFirestore db;
    private CollectionReference donHangRef;
    private CollectionReference usersRef;
    private CollectionReference khachHangRef;
    private CollectionReference congNoRef;

    // Data Holders (Lưu trữ dữ liệu cho việc xuất file)
    private List<Map<String, Object>> revenueDataForExport = new ArrayList<>();

    // Date Filter
    private Calendar currentFilterDate;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());


    // Định nghĩa màu sắc (Giữ nguyên)
    public static final int COLOR_REVENUE = Color.parseColor("#4CBB17");
    public static final int COLOR_PROFIT = Color.parseColor("#FF8C00");
    public static final int COLOR_TOUR_COMPLETED = Color.parseColor("#3B82F6");
    public static final int COLOR_TOUR_UPCOMING = Color.parseColor("#80D8FF");
    public static final int COLOR_DEBT_PAYABLE = Color.parseColor("#EF4444");
    public static final int COLOR_DEBT_RECEIVABLE = Color.parseColor("#FBBF24");
    public static final int COLOR_CUSTOMER_NEW = Color.parseColor("#8B5CF6");
    public static final int COLOR_CUSTOMER_OLD = Color.parseColor("#A78BFA");
    public static final int COLOR_CUSTOMER_VIP = Color.parseColor("#EC4899");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        donHangRef = db.collection("DonHang");
        usersRef = db.collection("users");
        khachHangRef = db.collection("khachhang");
        congNoRef = db.collection("CongNo");

        currentFilterDate = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);
        setupFilterLogic();
        setupChartsInitialConfig();
        setupTabLayoutListener();

        loadData();

        if (tabLayout != null && tabLayout.getTabCount() > 0) {
            tabLayout.getTabAt(0).select();
        }

        return view;
    }

    // --- KHỐI HÀM KHỞI TẠO VÀ LỌC THỜI GIAN ---
    private void initViews(View view) {
        textMonthFilter = view.findViewById(R.id.text_month_filter);
        ImageView iconDownload = view.findViewById(R.id.icon_download);
        tabLayout = view.findViewById(R.id.tabLayout);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        lineChartRevenue = view.findViewById(R.id.lineChartRevenue);
        barChartTours = view.findViewById(R.id.barChartTours);
        pieChartCustomers = view.findViewById(R.id.pieChartCustomers);
        pieChartDebt = view.findViewById(R.id.pieChartDebt);
        barChartPerformance = view.findViewById(R.id.barChartPerformance);
        cardRevenue = view.findViewById(R.id.cardRevenue);
        cardTours = view.findViewById(R.id.cardTours);
        gridCustomersDebt = view.findViewById(R.id.gridCustomersDebt);
        cardPerformance = view.findViewById(R.id.cardPerformance);

        if (iconDownload != null) {
            // Đã gắn chức năng Export
            iconDownload.setOnClickListener(v -> exportDataToCsv());
        }
    }

    private void setupFilterLogic() {
        textMonthFilter.setText(monthYearFormat.format(currentFilterDate.getTime()));
        textMonthFilter.setOnClickListener(v -> showMonthYearPicker());
    }

    private void showMonthYearPicker() {
        if (getContext() == null) return;

        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    currentFilterDate.set(Calendar.YEAR, year);
                    currentFilterDate.set(Calendar.MONTH, month);

                    textMonthFilter.setText(monthYearFormat.format(currentFilterDate.getTime()));
                    loadData();
                },
                currentFilterDate.get(Calendar.YEAR),
                currentFilterDate.get(Calendar.MONTH),
                currentFilterDate.get(Calendar.DAY_OF_MONTH));

        // SỬA LỖI NullPointerException TẠI ĐÂY
        View dayView = dialog.getDatePicker().findViewById(
                getResources().getIdentifier("day", "id", "android"));

        if (dayView != null) {
            // Ẩn view Ngày (chỉ giữ lại Tháng và Năm)
            dayView.setVisibility(View.GONE);
        } else {
            // Thêm log cảnh báo nếu không tìm thấy view "day"
            Log.w(TAG, "Không tìm thấy View 'day' trong DatePicker. Lọc ngày sẽ không hoạt động trên thiết bị này.");
        }
        // KẾT THÚC SỬA LỖI

        dialog.show();
    }

    private void loadData() {
        Calendar start = (Calendar) currentFilterDate.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);

        Date startDate = start.getTime();
        Date endDate = end.getTime();

        loadRevenueAndProfitData(startDate, endDate);
        loadTourOrderData(startDate, endDate); // Chỉ tính tour nội địa
        loadCustomerData();
        loadDebtData();
        loadPerformanceData(startDate, endDate);
    }
    // --- (END OF KHỐI HÀM LỌC) ---


    // --- 1. Tải Doanh thu & Lợi nhuận (LƯU TRỮ DỮ LIỆU ĐỂ EXPORT) ---
    private void loadRevenueAndProfitData(Date startDate, Date endDate) {
        donHangRef.whereGreaterThanOrEqualTo("ngayDat", startDate)
                .whereLessThanOrEqualTo("ngayDat", endDate)
                .orderBy("ngayDat", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, AtomicLong> dailyRevenue = new HashMap<>();
                    Map<String, AtomicLong> dailyProfit = new HashMap<>();

                    revenueDataForExport.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Date date = document.getDate("ngayDat");
                            Long totalAmount = document.getLong("tongTien");
                            Long profit = document.getLong("loiNhuan");
                            String orderId = document.getId();
                            String status = document.getString("trangThai");

                            if (date != null && totalAmount != null && profit != null) {
                                String dayKey = dayFormat.format(date);

                                dailyRevenue.computeIfAbsent(dayKey, k -> new AtomicLong(0)).addAndGet(totalAmount);
                                dailyProfit.computeIfAbsent(dayKey, k -> new AtomicLong(0)).addAndGet(profit);

                                // Lưu trữ dữ liệu chi tiết cho việc xuất file
                                Map<String, Object> orderDetails = new HashMap<>();
                                orderDetails.put("Mã ĐH", orderId);
                                orderDetails.put("Ngày", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date));
                                orderDetails.put("Tổng tiền", totalAmount);
                                orderDetails.put("Lợi nhuận", profit);
                                orderDetails.put("Trạng thái", status);
                                revenueDataForExport.add(orderDetails);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc dữ liệu Đơn hàng cho Doanh thu/Lợi nhuận: ", e);
                        }
                    }

                    // ... (Logic vẽ biểu đồ) ...
                    List<Entry> revenueEntries = new ArrayList<>();
                    List<Entry> profitEntries = new ArrayList<>();
                    List<String> xLabels = new ArrayList<>();

                    Calendar tempCal = (Calendar) currentFilterDate.clone();
                    tempCal.set(Calendar.DAY_OF_MONTH, 1);
                    int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

                    for (int i = 1; i <= daysInMonth; i++) {
                        String dayKey = String.format(Locale.getDefault(), "%02d", i);
                        xLabels.add(dayKey);

                        float revenue = dailyRevenue.getOrDefault(dayKey, new AtomicLong(0)).floatValue() / 1000000f;
                        float profit = dailyProfit.getOrDefault(dayKey, new AtomicLong(0)).floatValue() / 1000000f;

                        revenueEntries.add(new Entry(i, revenue));
                        profitEntries.add(new Entry(i, profit));
                    }

                    if (!revenueEntries.isEmpty()) {
                        setLineChartRevenueData(revenueEntries, profitEntries, xLabels);
                    } else {
                        lineChartRevenue.clear();
                        lineChartRevenue.setNoDataText("Không có dữ liệu Doanh thu trong tháng này.");
                        lineChartRevenue.invalidate();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải dữ liệu Doanh thu/Lợi nhuận:", e));
    }

    // --- 2. Tải Tour/Đặt chỗ (CHỈ NỘI ĐỊA) ---
    private void loadTourOrderData(Date startDate, Date endDate) {
        donHangRef.whereGreaterThanOrEqualTo("ngayDat", startDate)
                .whereLessThanOrEqualTo("ngayDat", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int completedCount = 0;
                    int upcomingCount = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Không cần check loaiTour, vì tất cả đều là tour trong nước
                            String trangThai = document.getString("trangThai");

                            if ("Completed".equalsIgnoreCase(trangThai)) {
                                completedCount++;
                            } else if ("Upcoming".equalsIgnoreCase(trangThai) || "Pending".equalsIgnoreCase(trangThai)) {
                                upcomingCount++;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc dữ liệu Đơn hàng:", e);
                        }
                    }

                    List<BarEntry> completedEntries = new ArrayList<>();
                    completedEntries.add(new BarEntry(1f, completedCount));

                    List<BarEntry> upcomingEntries = new ArrayList<>();
                    upcomingEntries.add(new BarEntry(2f, upcomingCount));

                    setBarChartToursData(completedEntries, upcomingEntries);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải dữ liệu Đặt chỗ:", e));
    }

    // --- CHỨC NĂNG XUẤT/TẢI XUỐNG FILE CSV ---

    private void exportDataToCsv() {
        if (revenueDataForExport.isEmpty() || getContext() == null) {
            Toast.makeText(getContext(), "Không có dữ liệu Đơn hàng để xuất cho tháng " + monthYearFormat.format(currentFilterDate.getTime()), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Ghi vào thư mục filesDir của ứng dụng (internal storage)
            String fileName = "BaoCaoDoanhThu_" + monthYearFormat.format(currentFilterDate.getTime()).replace("/", "_") + ".csv";
            File file = new File(getContext().getFilesDir(), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos);

            // Ghi Header
            writer.append("Mã ĐH,Ngày,Tổng tiền,Lợi nhuận,Trạng thái\n");

            // Ghi Dữ liệu
            for (Map<String, Object> order : revenueDataForExport) {
                writer.append(order.get("Mã ĐH").toString()).append(",");
                writer.append(order.get("Ngày").toString()).append(",");
                writer.append(order.get("Tổng tiền").toString()).append(",");
                writer.append(order.get("Lợi nhuận").toString()).append(",");
                writer.append(order.get("Trạng thái").toString()).append("\n");
            }

            writer.flush();
            writer.close();
            fos.close();

            // Thông báo thành công
            Toast.makeText(getContext(),
                    "Đã xuất dữ liệu thành công ra file:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "File exported to: " + file.getAbsolutePath());


        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xuất file CSV: ", e);
            Toast.makeText(getContext(), "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- CÁC HÀM TẢI DATA KHÁC (GIỮ NGUYÊN) ---

    private void loadCustomerData() {
        khachHangRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newCustomerCount = 0;
                    int loyalCustomerCount = 0;
                    int vipCustomerCount = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String loaiKhach = document.getString("loaiKhach");
                            if (loaiKhach != null) {
                                if (loaiKhach.equalsIgnoreCase("Mới")) {
                                    newCustomerCount++;
                                } else if (loaiKhach.equalsIgnoreCase("Thân thiết")) {
                                    loyalCustomerCount++;
                                } else if (loaiKhach.equalsIgnoreCase("VIP")) {
                                    vipCustomerCount++;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc dữ liệu Khách hàng:", e);
                        }
                    }

                    List<PieEntry> entries = new ArrayList<>();
                    if (newCustomerCount > 0) entries.add(new PieEntry(newCustomerCount, "Khách mới"));
                    if (loyalCustomerCount > 0) entries.add(new PieEntry(loyalCustomerCount, "Khách thân thiết"));
                    if (vipCustomerCount > 0) entries.add(new PieEntry(vipCustomerCount, "Khách VIP"));

                    if (!entries.isEmpty()) {
                        setPieChartCustomersData(entries);
                    } else {
                        pieChartCustomers.clear();
                        pieChartCustomers.setNoDataText("Không có dữ liệu Khách hàng.");
                        pieChartCustomers.invalidate();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải dữ liệu Khách hàng:", e));
    }

    private void loadDebtData() {
        congNoRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long phaiThuTotal = 0;
                    long phaiTraTotal = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String loai = document.getString("loaiCongNo");
                            Long amount = document.getLong("tongTien");

                            if (loai != null && amount != null) {
                                if (loai.equalsIgnoreCase("PhaiThu")) {
                                    phaiThuTotal += amount;
                                } else if (loai.equalsIgnoreCase("PhaiTra")) {
                                    phaiTraTotal += amount;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc dữ liệu Công nợ:", e);
                        }
                    }

                    List<PieEntry> entries = new ArrayList<>();
                    if (phaiTraTotal > 0) entries.add(new PieEntry(phaiTraTotal / 1000000f, "Nợ phải trả"));
                    if (phaiThuTotal > 0) entries.add(new PieEntry(phaiThuTotal / 1000000f, "Nợ phải thu"));

                    if (!entries.isEmpty()) {
                        setPieChartDebtData(entries);
                    } else {
                        pieChartDebt.clear();
                        pieChartDebt.setNoDataText("Không có dữ liệu Công nợ.");
                        pieChartDebt.invalidate();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải dữ liệu Công nợ:", e));
    }

    private void loadPerformanceData(Date startDate, Date endDate) {
        usersRef.whereEqualTo("role", "NhanVien")
                .get()
                .addOnSuccessListener(userSnapshots -> {
                    List<String> nvNames = new ArrayList<>();
                    List<Long> nvSales = new ArrayList<>();

                    for (QueryDocumentSnapshot userDoc : userSnapshots) {
                        try {
                            String nvName = userDoc.getString("tenNhanVien");
                            Long sales = userDoc.getLong("totalSales");

                            if (nvName != null && sales != null) {
                                nvNames.add(nvName);
                                nvSales.add(sales);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc dữ liệu Nhân viên:", e);
                        }
                    }

                    List<BarEntry> performanceEntries = new ArrayList<>();
                    for (int i = 0; i < nvSales.size(); i++) {
                        float ratio = Math.min(1f, nvSales.get(i).floatValue() / 100f);
                        performanceEntries.add(new BarEntry(i + 1f, ratio));
                    }

                    if (!performanceEntries.isEmpty()) {
                        setBarChartPerformanceData(performanceEntries, nvNames);
                    } else {
                        barChartPerformance.clear();
                        barChartPerformance.setNoDataText("Không có dữ liệu Hiệu suất NV.");
                        barChartPerformance.invalidate();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải dữ liệu Hiệu suất NV:", e));
    }

    // --- CÁC HÀM SET DATA VÀ VẼ BIỂU ĐỒ (GIỮ NGUYÊN) ---

    private void setupChartsInitialConfig() {
        lineChartRevenue.getDescription().setEnabled(false);
        lineChartRevenue.getAxisRight().setEnabled(false);
        lineChartRevenue.getAxisLeft().setAxisMinimum(0f);
        lineChartRevenue.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartRevenue.getXAxis().setGranularity(1f);
        lineChartRevenue.getXAxis().setDrawGridLines(false);

        barChartTours.getDescription().setEnabled(false);
        barChartTours.getAxisRight().setEnabled(false);
        barChartTours.getAxisLeft().setAxisMinimum(0f);
        barChartTours.getLegend().setEnabled(true);
        barChartTours.setFitBars(true);

        pieChartCustomers.getDescription().setEnabled(false);
        pieChartCustomers.setUsePercentValues(true);
        pieChartCustomers.setEntryLabelColor(Color.WHITE);
        pieChartCustomers.setHoleRadius(58f);
        pieChartCustomers.setTransparentCircleRadius(61f);
        pieChartCustomers.setDrawEntryLabels(false);

        pieChartDebt.getDescription().setEnabled(false);
        pieChartDebt.setUsePercentValues(true);
        pieChartDebt.setEntryLabelColor(Color.WHITE);
        pieChartDebt.setHoleRadius(58f);
        pieChartDebt.setTransparentCircleRadius(61f);
        pieChartDebt.setDrawEntryLabels(false);

        barChartPerformance.getDescription().setEnabled(false);
        barChartPerformance.getAxisRight().setEnabled(false);
        barChartPerformance.getAxisLeft().setAxisMinimum(0f);
        barChartPerformance.getAxisLeft().setAxisMaximum(1f);
        barChartPerformance.setFitBars(true);
        barChartPerformance.getLegend().setEnabled(false);
        barChartPerformance.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartPerformance.getXAxis().setDrawGridLines(false);
    }

    private void setLineChartRevenueData(List<Entry> revenueEntries, List<Entry> profitEntries, List<String> xLabels) {
        LineDataSet revenueDataSet = new LineDataSet(revenueEntries, "Doanh thu (Triệu)");
        revenueDataSet.setColor(COLOR_REVENUE);
        revenueDataSet.setDrawCircles(true);
        revenueDataSet.setCircleColor(COLOR_REVENUE);
        revenueDataSet.setDrawCircleHole(false);
        revenueDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        revenueDataSet.setLineWidth(2f);
        revenueDataSet.setDrawFilled(true);
        revenueDataSet.setFillColor(Color.parseColor("#D4EDDA"));

        LineDataSet profitDataSet = new LineDataSet(profitEntries, "Lợi nhuận (Triệu)");
        profitDataSet.setColor(COLOR_PROFIT);
        profitDataSet.setDrawCircles(true);
        profitDataSet.setCircleColor(COLOR_PROFIT);
        profitDataSet.setDrawCircleHole(false);
        profitDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        profitDataSet.setLineWidth(2f);
        profitDataSet.setDrawFilled(true);
        profitDataSet.setFillColor(Color.parseColor("#FDE7C9"));

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(revenueDataSet);
        dataSets.add(profitDataSet);

        LineData lineData = new LineData(dataSets);
        lineChartRevenue.setData(lineData);

        XAxis xAxis = lineChartRevenue.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setAxisMinimum(1f);
        xAxis.setAxisMaximum(xLabels.size());

        lineChartRevenue.animateX(1000);
        lineChartRevenue.invalidate();
    }

    private void setBarChartToursData(List<BarEntry> completedEntries, List<BarEntry> upcomingEntries) {
        BarDataSet completedDataSet = new BarDataSet(completedEntries, "Tour đã hoàn thành");
        completedDataSet.setColor(COLOR_TOUR_COMPLETED);

        BarDataSet upcomingDataSet = new BarDataSet(upcomingEntries, "Tour sắp diễn ra");
        upcomingDataSet.setColor(COLOR_TOUR_UPCOMING);

        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(completedDataSet);
        dataSets.add(upcomingDataSet);

        BarData barData = new BarData(dataSets);

        float groupSpace = 0.08f;
        float barSpace = 0.02f;
        float barWidth = 0.45f;

        barData.setBarWidth(barWidth);

        barChartTours.setData(barData);
        barChartTours.groupBars(0.5f, groupSpace, barSpace);

        final String[] tourTypes = new String[]{"", "Tổng quan"};
        XAxis xAxis = barChartTours.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(tourTypes));
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0.5f);
        xAxis.setAxisMaximum(1.5f);

        barChartTours.animateY(1000);
        barChartTours.invalidate();
    }

    private void setPieChartCustomersData(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(COLOR_CUSTOMER_NEW);
        colors.add(COLOR_CUSTOMER_OLD);
        colors.add(COLOR_CUSTOMER_VIP);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);
        PieData data = new PieData(dataSet);
        pieChartCustomers.setData(data);
        pieChartCustomers.setCenterText("Khách hàng\n(Theo %)");
        pieChartCustomers.animateY(1000);
        Legend legend = pieChartCustomers.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        pieChartCustomers.invalidate();
    }

    private void setPieChartDebtData(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(COLOR_DEBT_PAYABLE);
        colors.add(COLOR_DEBT_RECEIVABLE);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);
        PieData data = new PieData(dataSet);
        pieChartDebt.setData(data);
        pieChartDebt.setCenterText("Công nợ\n(Theo Triệu)");
        pieChartDebt.animateY(1000);
        Legend legend = pieChartDebt.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        pieChartDebt.invalidate();
    }

    private void setBarChartPerformanceData(List<BarEntry> performanceEntries, List<String> nvNames) {
        BarDataSet dataSet = new BarDataSet(performanceEntries, "Tỉ lệ hoàn thành mục tiêu");
        dataSet.setColor(COLOR_REVENUE);
        dataSet.setValueTextSize(10f);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChartPerformance.setData(barData);
        XAxis xAxis = barChartPerformance.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(nvNames));
        xAxis.setAxisMinimum(0.5f);
        xAxis.setAxisMaximum(performanceEntries.size() + 0.5f);
        barChartPerformance.animateY(1000);
        barChartPerformance.invalidate();
    }

    private void setupTabLayoutListener() {
        if (tabLayout == null) return;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterChartsByPosition(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* No op */ }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                scrollToView(getCardViewByPosition(tab.getPosition()));
            }
        });
    }

    private void filterChartsByPosition(int position) {
        hideAllCards();
        View targetView = getCardViewByPosition(position);
        if (targetView != null) {
            targetView.setVisibility(View.VISIBLE);
            scrollToView(targetView);
        } else if (position == 0) {
            showAllCards();
            scrollToView(cardRevenue);
        }
    }

    private View getCardViewByPosition(int position) {
        switch (position) {
            case 0: return cardRevenue;
            case 1: return cardTours;
            case 2: return cardPerformance;
            case 3: return gridCustomersDebt;
            default: return null;
        }
    }

    private void hideAllCards() {
        if (cardRevenue != null) cardRevenue.setVisibility(View.GONE);
        if (cardTours != null) cardTours.setVisibility(View.GONE);
        if (cardPerformance != null) cardPerformance.setVisibility(View.GONE);
        if (gridCustomersDebt != null) gridCustomersDebt.setVisibility(View.GONE);
    }

    private void showAllCards() {
        if (cardRevenue != null) cardRevenue.setVisibility(View.VISIBLE);
        if (cardTours != null) cardTours.setVisibility(View.VISIBLE);
        if (cardPerformance != null) cardPerformance.setVisibility(View.VISIBLE);
        if (gridCustomersDebt != null) gridCustomersDebt.setVisibility(View.VISIBLE);
    }

    private void scrollToView(final View targetView) {
        if (targetView != null && nestedScrollView != null) {
            nestedScrollView.post(() -> nestedScrollView.scrollTo(
                    0,
                    targetView.getTop()
            ));
        }
    }
}