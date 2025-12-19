package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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

import com.example.quanlytourdl.model.CongNoModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Các import cho iText7 PDF
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private TextView textMonthFilter;
    private TabLayout tabLayout;
    private NestedScrollView nestedScrollView;
    private LineChart lineChartRevenue;
    private BarChart barChartTours, barChartPerformance;
    private PieChart pieChartCustomers, pieChartDebt;
    private CardView cardRevenue, cardTours, cardPerformance;
    private GridLayout gridCustomersDebt;

    private FirebaseFirestore db;
    private CollectionReference donHangRef, khachHangRef, congNoRef, payrollsRef, rewardRef;
    private List<ListenerRegistration> activeListeners = new ArrayList<>();

    private List<String[]> dataToExport = new ArrayList<>();
    private Calendar currentFilterDate;

    private long totalRevenue = 0;
    private long totalExpense = 0;

    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
    private SimpleDateFormat payrollPeriodFormat = new SimpleDateFormat("'tháng' M 'năm' yyyy", Locale.getDefault());
    private DecimalFormat currencyFormat = new DecimalFormat("#,### VNĐ");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        donHangRef = db.collection("DonHang");
        khachHangRef = db.collection("khachhang");
        congNoRef = db.collection("CongNo");
        payrollsRef = db.collection("Payrolls");
        rewardRef = db.collection("RewardPunishment");
        currentFilterDate = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupChartsInitialConfig();
        setupFilterLogic();
        setupTabLayoutListener();
        loadAllDataRealtime();
        return view;
    }

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

        // ĐỔI TỪ CSV SANG PDF TẠI ĐÂY
        if (iconDownload != null) iconDownload.setOnClickListener(v -> exportDataToPdf());
    }

    private void setupChartsInitialConfig() {
        lineChartRevenue.getDescription().setEnabled(false);
        barChartTours.getDescription().setEnabled(false);
        pieChartCustomers.getDescription().setEnabled(false);
        pieChartDebt.getDescription().setEnabled(false);
        barChartPerformance.getDescription().setEnabled(false);

        XAxis perfX = barChartPerformance.getXAxis();
        perfX.setPosition(XAxis.XAxisPosition.BOTTOM);
        perfX.setGranularity(1f);
        perfX.setDrawGridLines(false);
        perfX.setLabelRotationAngle(-45f);
        barChartPerformance.setExtraBottomOffset(60f);
    }

    private void loadAllDataRealtime() {
        for (ListenerRegistration lr : activeListeners) lr.remove();
        activeListeners.clear();

        dataToExport.clear();
        dataToExport.add(new String[]{"LOẠI", "TÊN/MÃ", "GIÁ TRỊ", "TRẠNG THÁI"});

        totalRevenue = 0;
        totalExpense = 0;

        Calendar start = (Calendar) currentFilterDate.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);

        activeListeners.add(donHangRef.whereGreaterThanOrEqualTo("ngayTao", start.getTime())
                .whereLessThan("ngayTao", end.getTime())
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;
                    Map<String, AtomicLong> dailyRev = new HashMap<>();
                    long monthlyRev = 0;
                    int dangXuLy = 0, choDuyet = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        Long total = doc.getLong("tongTien");
                        String status = String.valueOf(doc.get("trangThai"));
                        if (total != null) {
                            monthlyRev += total;
                            dataToExport.add(new String[]{"DOANH THU", "Đơn: " + doc.getId().substring(0,6), currencyFormat.format(total), "Thành công"});
                            Timestamp ts = doc.getTimestamp("ngayTao");
                            if (ts != null) dailyRev.computeIfAbsent(dayFormat.format(ts.toDate()), k -> new AtomicLong(0)).addAndGet(total);
                        }
                        if ("CHO_XU_LY".equalsIgnoreCase(status) || "0".equals(status)) choDuyet++; else dangXuLy++;
                    }
                    totalRevenue = monthlyRev;
                    updateRevenueAndProfitChart(dailyRev);
                    setBarChartToursData(dangXuLy, choDuyet);
                }));

        activeListeners.add(congNoRef.addSnapshotListener((value, error) -> {
            if (value == null) return;
            double thu = 0, tra = 0;
            for (QueryDocumentSnapshot doc : value) {
                CongNoModel m = doc.toObject(CongNoModel.class);
                String dv = m.getLoaiDichVu() != null ? m.getLoaiDichVu().toLowerCase() : "";
                if (dv.contains("khách") || dv.contains("tour")) thu += m.getSoTien();
                else {
                    tra += m.getSoTien();
                    dataToExport.add(new String[]{"CHI PHÍ", m.getNoiDung(), currencyFormat.format(m.getSoTien()), "Công nợ"});
                }
            }
            totalExpense = (long) tra;
            updateDebtPieChart(thu, tra);
        }));

        activeListeners.add(khachHangRef.addSnapshotListener((value, error) -> {
            if (value == null) return;
            int m = 0, c = 0, v = 0;
            for (QueryDocumentSnapshot doc : value) {
                String loai = doc.getString("loaiKhach");
                if ("Mới".equalsIgnoreCase(loai)) m++; else if ("VIP".equalsIgnoreCase(loai)) v++; else c++;
            }
            updateCustomerPie(m, c, v);
        }));

        loadStaffPerformanceRealtime();
    }

    private void loadStaffPerformanceRealtime() {
        String currentPeriod = payrollPeriodFormat.format(currentFilterDate.getTime());
        activeListeners.add(payrollsRef.whereEqualTo("salaryPeriod", currentPeriod)
                .addSnapshotListener((payrolls, error) -> {
                    if (payrolls == null) return;
                    Map<String, Double> perfMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : payrolls) {
                        String name = doc.getString("tenNhanVien");
                        double val = (doc.getDouble("thuongHoaHong") != null ? doc.getDouble("thuongHoaHong") : 0)
                                - (doc.getDouble("phatKhauTru") != null ? doc.getDouble("phatKhauTru") : 0);
                        if (name != null) perfMap.put(name, val);
                    }

                    rewardRef.whereEqualTo("status", "Đã phê duyệt").addSnapshotListener((rewards, err2) -> {
                        Map<String, Double> finalMap = new HashMap<>(perfMap);
                        if (rewards != null) {
                            for (QueryDocumentSnapshot doc : rewards) {
                                String name = doc.getString("employeeName");
                                Double amt = doc.getDouble("amount");
                                String type = doc.getString("type");
                                if (name != null && amt != null) {
                                    double cur = finalMap.getOrDefault(name, 0.0);
                                    finalMap.put(name, "Thưởng".equalsIgnoreCase(type) ? cur + amt : cur - amt);
                                }
                            }
                        }
                        updatePerformanceBarChart(finalMap);
                    });
                }));
    }

    private void updatePerformanceBarChart(Map<String, Double> data) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> staffNames = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new BarEntry(index, (float) (entry.getValue() / 1_000_000.0)));
            staffNames.add(entry.getKey());
            index++;
        }

        if (entries.isEmpty()) {
            barChartPerformance.clear();
            return;
        }

        BarDataSet set = new BarDataSet(entries, "Hiệu suất (Triệu VNĐ)");
        set.setColors(Color.parseColor("#6366F1"), Color.parseColor("#10B981"));
        set.setValueTextSize(10f);

        BarData barData = new BarData(set);
        barData.setBarWidth(0.5f);

        barChartPerformance.setData(barData);
        XAxis xAxis = barChartPerformance.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(staffNames));
        xAxis.setLabelCount(staffNames.size());
        barChartPerformance.animateY(1000);
        barChartPerformance.invalidate();
    }

    private void setBarChartToursData(int dangXuLy, int choDuyet) {
        List<BarEntry> e1 = new ArrayList<>(); e1.add(new BarEntry(1f, dangXuLy));
        List<BarEntry> e2 = new ArrayList<>(); e2.add(new BarEntry(2f, choDuyet));
        BarDataSet d1 = new BarDataSet(e1, "Đang xử lý"); d1.setColor(Color.parseColor("#3B82F6"));
        BarDataSet d2 = new BarDataSet(e2, "Chờ xử lý"); d2.setColor(Color.parseColor("#FFCC00"));
        barChartTours.setData(new BarData(d1, d2));
        barChartTours.invalidate();
    }

    private void updateRevenueAndProfitChart(Map<String, AtomicLong> dailyRev) {
        List<Entry> revEntries = new ArrayList<>();
        List<Entry> profitEntries = new ArrayList<>();
        int days = currentFilterDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= days; i++) {
            String key = String.format(Locale.getDefault(), "%02d", i);
            float rev = dailyRev.getOrDefault(key, new AtomicLong(0)).floatValue() / 1_000_000f;
            revEntries.add(new Entry(i, rev));
            profitEntries.add(new Entry(i, rev * 0.5f));
        }
        LineDataSet s1 = new LineDataSet(revEntries, "Doanh thu (Trđ)");
        s1.setColor(Color.GREEN); s1.setDrawFilled(true); s1.setFillColor(Color.parseColor("#D4EDDA"));
        LineDataSet s2 = new LineDataSet(profitEntries, "Lợi nhuận (Trđ)");
        s2.setColor(Color.BLUE);
        lineChartRevenue.setData(new LineData(s1, s2));
        lineChartRevenue.invalidate();
    }

    private void updateCustomerPie(int n, int l, int v) {
        List<PieEntry> entries = new ArrayList<>();
        if (n > 0) entries.add(new PieEntry(n, "Mới"));
        if (l > 0) entries.add(new PieEntry(l, "Cũ"));
        if (v > 0) entries.add(new PieEntry(v, "VIP"));
        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(Color.parseColor("#8B5CF6"), Color.parseColor("#A78BFA"), Color.parseColor("#EC4899"));
        pieChartCustomers.setData(new PieData(set));
        pieChartCustomers.setCenterText("Khách hàng");
        pieChartCustomers.invalidate();
    }

    private void updateDebtPieChart(double thu, double tra) {
        List<PieEntry> entries = new ArrayList<>();
        if (thu > 0) entries.add(new PieEntry((float)(thu/1_000_000), "Thu"));
        if (tra > 0) entries.add(new PieEntry((float)(tra/1_000_000), "Trả"));
        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(Color.parseColor("#10B981"), Color.parseColor("#EF4444"));
        pieChartDebt.setData(new PieData(set));
        pieChartDebt.invalidate();
    }

    // HÀM XUẤT PDF MỚI
    private void exportDataToPdf() {
        if (dataToExport.size() <= 1) {
            Toast.makeText(getContext(), "Không có dữ liệu để xuất!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "BaoCao_" + System.currentTimeMillis() + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Tiêu đề
            document.add(new Paragraph("BÁO CÁO THỐNG KÊ DOANH NGHIỆP")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold());
            document.add(new Paragraph("Tháng báo cáo: " + monthYearFormat.format(currentFilterDate.getTime()))
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Tạo bảng 4 cột
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 3, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Thêm Header
            for (String header : dataToExport.get(0)) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Thêm dữ liệu
            for (int i = 1; i < dataToExport.size(); i++) {
                for (String cellContent : dataToExport.get(i)) {
                    table.addCell(new Cell().add(new Paragraph(cellContent))
                            .setTextAlignment(TextAlignment.CENTER));
                }
            }

            document.add(table);

            // Tổng kết cuối
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("TỔNG DOANH THU: " + currencyFormat.format(totalRevenue)).setBold());
            document.add(new Paragraph("TỔNG CHI PHÍ: " + currencyFormat.format(totalExpense)).setBold());
            long finalProfit = totalRevenue - totalExpense;
            document.add(new Paragraph("LỢI NHUẬN RÒNG: " + currencyFormat.format(finalProfit))
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(finalProfit >= 0 ? ColorConstants.GREEN : ColorConstants.RED));

            document.close();
            Toast.makeText(getContext(), "Đã xuất PDF thành công vào thư mục Downloads!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi xuất PDF: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi khi tạo file PDF!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFilterLogic() {
        textMonthFilter.setText(monthYearFormat.format(currentFilterDate.getTime()));
        textMonthFilter.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (dp, y, m, d) -> {
                currentFilterDate.set(y, m, 1);
                textMonthFilter.setText(monthYearFormat.format(currentFilterDate.getTime()));
                loadAllDataRealtime();
            }, currentFilterDate.get(Calendar.YEAR), currentFilterDate.get(Calendar.MONTH), 1).show();
        });
    }

    private void setupTabLayoutListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { filterCharts(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterCharts(int pos) {
        cardRevenue.setVisibility(pos == 0 || pos == 1 ? View.VISIBLE : View.GONE);
        cardTours.setVisibility(pos == 0 || pos == 2 ? View.VISIBLE : View.GONE);
        gridCustomersDebt.setVisibility(pos == 0 || pos == 3 ? View.VISIBLE : View.GONE);
        cardPerformance.setVisibility(pos == 0 || pos == 4 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        for (ListenerRegistration lr : activeListeners) lr.remove();
        super.onDestroy();
    }
}