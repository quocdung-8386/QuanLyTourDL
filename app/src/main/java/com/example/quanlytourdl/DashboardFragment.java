package com.example.quanlytourdl;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
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

import java.util.ArrayList;
import java.util.List;

// Đảm bảo R được import đúng package
// Ví dụ: import com.example.quanlytourdl.R;

public class DashboardFragment extends Fragment {

    private LineChart lineChartRevenue;
    private BarChart barChartTours;
    private PieChart pieChartCustomers;
    private PieChart pieChartDebt; // Biến này cần được sử dụng
    private BarChart barChartPerformance;

    // Định nghĩa màu sắc tối ưu hơn
    public static final int COLOR_REVENUE = Color.parseColor("#4CBB17");  // Xanh lá (Doanh thu)
    public static final int COLOR_PROFIT = Color.parseColor("#FF8C00");   // Cam (Lợi nhuận)
    public static final int COLOR_TOUR_COMPLETED = Color.parseColor("#3B82F6"); // Xanh dương
    public static final int COLOR_TOUR_UPCOMING = Color.parseColor("#80D8FF");  // Xanh dương nhạt
    public static final int COLOR_DEBT_PAYABLE = Color.parseColor("#EF4444"); // Đỏ (Nợ phải trả)
    public static final int COLOR_DEBT_RECEIVABLE = Color.parseColor("#FBBF24"); // Vàng (Nợ phải thu)
    public static final int COLOR_CUSTOMER_NEW = Color.parseColor("#8B5CF6"); // Tím
    public static final int COLOR_CUSTOMER_OLD = Color.parseColor("#A78BFA"); // Tím nhạt
    public static final int COLOR_CUSTOMER_VIP = Color.parseColor("#EC4899"); // Hồng

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // SỬA LỖI: Sử dụng R.layout.fragment_dashboard của package hiện tại (giả sử đã import R)
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Ánh xạ các biểu đồ
        lineChartRevenue = view.findViewById(R.id.lineChartRevenue);
        barChartTours = view.findViewById(R.id.barChartTours);
        pieChartCustomers = view.findViewById(R.id.pieChartCustomers);
        pieChartDebt = view.findViewById(R.id.pieChartDebt); // Khởi tạo Công nợ
        barChartPerformance = view.findViewById(R.id.barChartPerformance);

        // Gọi các hàm để cài đặt và điền dữ liệu
        setupLineChartRevenue();
        setupBarChartTours();
        setupPieChartCustomers();
        setupPieChartDebt(); // Bổ sung hàm Công nợ
        setupBarChartPerformance();

        return view;
    }

    // --- 1. BIỂU ĐỒ ĐƯỜNG (DOANH THU & LỢI NHUẬN) ---
    private void setupLineChartRevenue() {
        // Dữ liệu giả định
        List<Entry> revenueEntries = new ArrayList<>();
        revenueEntries.add(new Entry(1f, 50f));
        revenueEntries.add(new Entry(2f, 65f));
        revenueEntries.add(new Entry(3f, 40f));
        revenueEntries.add(new Entry(4f, 85f));
        revenueEntries.add(new Entry(5f, 70f));
        revenueEntries.add(new Entry(6f, 55f));

        List<Entry> profitEntries = new ArrayList<>();
        profitEntries.add(new Entry(1f, 30f));
        profitEntries.add(new Entry(2f, 50f));
        profitEntries.add(new Entry(3f, 25f));
        profitEntries.add(new Entry(4f, 60f));
        profitEntries.add(new Entry(5f, 45f));
        profitEntries.add(new Entry(6f, 38f));

        LineDataSet revenueDataSet = new LineDataSet(revenueEntries, "Doanh thu");
        revenueDataSet.setColor(COLOR_REVENUE);
        revenueDataSet.setDrawCircles(false);
        revenueDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        revenueDataSet.setLineWidth(2f);
        revenueDataSet.setDrawFilled(true); // Thêm đổ màu dưới đường
        revenueDataSet.setFillColor(Color.parseColor("#D4EDDA")); // Xanh lá nhạt

        LineDataSet profitDataSet = new LineDataSet(profitEntries, "Lợi nhuận");
        profitDataSet.setColor(COLOR_PROFIT);
        profitDataSet.setDrawCircles(false);
        profitDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        profitDataSet.setLineWidth(2f);
        profitDataSet.setDrawFilled(true); // Thêm đổ màu dưới đường
        profitDataSet.setFillColor(Color.parseColor("#FDE7C9")); // Cam nhạt

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(revenueDataSet);
        dataSets.add(profitDataSet);

        LineData lineData = new LineData(dataSets);
        lineChartRevenue.setData(lineData);

        // Cài đặt trục X
        final String[] quarters = new String[]{"", "T1", "T2", "T3", "T4", "T5", "T6"};
        XAxis xAxis = lineChartRevenue.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(quarters));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        // Cài đặt chung
        lineChartRevenue.getDescription().setEnabled(false);
        lineChartRevenue.getAxisRight().setEnabled(false);
        lineChartRevenue.animateX(1000);
        lineChartRevenue.invalidate();
    }

    // --- 2. BIỂU ĐỒ CỘT (TOUR & ĐẶT CHỖ) ---
    private void setupBarChartTours() {
        // Dữ liệu giả định
        List<BarEntry> completedEntries = new ArrayList<>();
        completedEntries.add(new BarEntry(1f, 12f));
        completedEntries.add(new BarEntry(2f, 18f));
        completedEntries.add(new BarEntry(3f, 8f));
        completedEntries.add(new BarEntry(4f, 15f));
        completedEntries.add(new BarEntry(5f, 6f));
        completedEntries.add(new BarEntry(6f, 11f));

        List<BarEntry> upcomingEntries = new ArrayList<>();
        upcomingEntries.add(new BarEntry(1f, 8f));
        upcomingEntries.add(new BarEntry(2f, 12f));
        upcomingEntries.add(new BarEntry(3f, 14f));
        upcomingEntries.add(new BarEntry(4f, 8f));
        upcomingEntries.add(new BarEntry(5f, 10f));
        upcomingEntries.add(new BarEntry(6f, 16f));

        BarDataSet completedDataSet = new BarDataSet(completedEntries, "Tour đã hoàn thành");
        completedDataSet.setColor(COLOR_TOUR_COMPLETED);

        BarDataSet upcomingDataSet = new BarDataSet(upcomingEntries, "Tour sắp diễn ra");
        upcomingDataSet.setColor(COLOR_TOUR_UPCOMING);

        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(completedDataSet);
        dataSets.add(upcomingDataSet);

        BarData barData = new BarData(dataSets);

        // Cài đặt Group Bar (giữ nguyên cài đặt Group Bar của bạn)
        float groupSpace = 0.08f;
        float barSpace = 0.02f;
        float barWidth = 0.45f;
        barData.setBarWidth(barWidth);

        barChartTours.setData(barData);
        barChartTours.groupBars(1f, groupSpace, barSpace);

        // Cài đặt trục X
        final String[] months = new String[]{"", "T1", "T2", "T3", "T4", "T5", "T6"};
        XAxis xAxis = barChartTours.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(1f);
        xAxis.setAxisMaximum(barChartTours.getBarData().getGroupWidth(groupSpace, barSpace) * 6 + 1f);

        // Cài đặt chung
        barChartTours.getDescription().setEnabled(false);
        barChartTours.getAxisRight().setEnabled(false);
        barChartTours.animateY(1000);
        barChartTours.invalidate();
    }

    // --- 3. BIỂU ĐỒ TRÒN (KHÁCH HÀNG) ---
    private void setupPieChartCustomers() {
        // Dữ liệu giả định
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(45f, "Khách mới"));
        entries.add(new PieEntry(35f, "Khách cũ"));
        entries.add(new PieEntry(20f, "Khách VIP"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Cài đặt màu sắc
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(COLOR_CUSTOMER_NEW);
        colors.add(COLOR_CUSTOMER_OLD);
        colors.add(COLOR_CUSTOMER_VIP);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChartCustomers.setData(data);

        // Cài đặt Donut Chart và center text
        pieChartCustomers.getDescription().setEnabled(false);
        pieChartCustomers.setHoleRadius(58f); // Khoét lỗ giữa
        pieChartCustomers.setTransparentCircleRadius(61f);
        pieChartCustomers.setCenterText("Khách hàng\n(Theo %)"); // Sửa lại Center Text
        pieChartCustomers.setDrawEntryLabels(false);
        pieChartCustomers.animateY(1000);

        // Cài đặt Legend
        Legend legend = pieChartCustomers.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChartCustomers.invalidate();
    }

    // --- 4. BIỂU ĐỒ TRÒN (CÔNG NỢ) - BỔ SUNG ---
    private void setupPieChartDebt() {
        // Dữ liệu giả định
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(65f, "Nợ phải trả")); // 65%
        entries.add(new PieEntry(35f, "Nợ phải thu")); // 35%

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Cài đặt màu sắc
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(COLOR_DEBT_PAYABLE);
        colors.add(COLOR_DEBT_RECEIVABLE);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChartDebt.setData(data);

        // Cài đặt Donut Chart và center text
        pieChartDebt.getDescription().setEnabled(false);
        pieChartDebt.setHoleRadius(58f); // Khoét lỗ giữa
        pieChartDebt.setTransparentCircleRadius(61f);
        pieChartDebt.setCenterText("Công nợ\n(Theo %)");
        pieChartDebt.setDrawEntryLabels(false);
        pieChartDebt.animateY(1000);

        // Cài đặt Legend
        Legend legend = pieChartDebt.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChartDebt.invalidate();
    }

    // --- 5. BIỂU ĐỒ CỘT (HIỆU SUẤT NHÂN VIÊN) ---
    private void setupBarChartPerformance() {
        // Dữ liệu giả định
        List<BarEntry> performanceEntries = new ArrayList<>();
        performanceEntries.add(new BarEntry(1f, 0.8f));
        performanceEntries.add(new BarEntry(2f, 0.5f));
        performanceEntries.add(new BarEntry(3f, 0.9f));
        performanceEntries.add(new BarEntry(4f, 0.7f));
        performanceEntries.add(new BarEntry(5f, 0.6f));

        BarDataSet dataSet = new BarDataSet(performanceEntries, "Tỉ lệ hoàn thành mục tiêu");
        dataSet.setColor(COLOR_REVENUE); // Sử dụng màu xanh lá (Giả định là màu tốt)

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChartPerformance.setData(barData);

        // Cài đặt trục X
        final String[] employees = new String[]{"", "NV A", "NV B", "NV C", "NV D", "NV E"};
        XAxis xAxis = barChartPerformance.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(employees));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0.5f);
        xAxis.setAxisMaximum(5.5f);

        // Cài đặt trục Y (Tỉ lệ từ 0 đến 1)
        barChartPerformance.getAxisLeft().setAxisMinimum(0f);
        barChartPerformance.getAxisLeft().setAxisMaximum(1f);
        barChartPerformance.getAxisRight().setEnabled(false);

        // Cài đặt chung
        barChartPerformance.getDescription().setEnabled(false);
        barChartPerformance.getLegend().setEnabled(false);
        barChartPerformance.animateY(1000);
        barChartPerformance.invalidate();
    }
}