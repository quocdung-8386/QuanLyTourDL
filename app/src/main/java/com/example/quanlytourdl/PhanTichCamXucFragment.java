package com.example.quanlytourdl;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.FeedbackAdapter;
import com.example.quanlytourdl.model.Feedback;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhanTichCamXucFragment extends Fragment {

    private PieChart pieChart;
    private LineChart lineChart;
    private TextView tvTotal, tvRate;
    private RecyclerView recyclerView;
    private FeedbackAdapter adapter;
    private List<Feedback> mList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // [QUAN TRỌNG] Inflate đúng file layout mới đổi tên
        View view = inflater.inflate(R.layout.fragment_phan_tich_cam_xuc, container, false);
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupSpinners(view);
        setupCharts();
        setupRecyclerView();

        loadDataFromFirebase();

        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_sentiment);
        pieChart = view.findViewById(R.id.pieChart_sentiment);
        lineChart = view.findViewById(R.id.lineChart_trend);
        tvTotal = view.findViewById(R.id.tv_total_feedback);
        tvRate = view.findViewById(R.id.tv_satisfaction_rate);
        recyclerView = view.findViewById(R.id.rv_feedback_list);
        fabAdd = view.findViewById(R.id.fab_add_feedback);

        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        fabAdd.setOnClickListener(v -> showAddFeedbackDialog());
    }

    private void setupSpinners(View view) {
        Spinner spTour = view.findViewById(R.id.sp_filter_tour);
        ArrayAdapter<String> adapterTour = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Tất cả tour", "Đà Nẵng", "Hạ Long"});
        spTour.setAdapter(adapterTour);

        Spinner spTime = view.findViewById(R.id.sp_filter_time);
        ArrayAdapter<String> adapterTime = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Tháng này", "Tháng trước", "Năm nay"});
        spTime.setAdapter(adapterTime);
    }

    private void setupCharts() {
        // --- Pie Chart Config ---
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.GRAY);

        // --- Line Chart Config ---
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(true);
    }

    private void setupRecyclerView() {
        mList = new ArrayList<>();
        adapter = new FeedbackAdapter(mList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadDataFromFirebase() {
        db.collection("Feedbacks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        mList.clear();
                        int total = 0;
                        int positive = 0;
                        int neutral = 0;
                        int negative = 0;
                        int satisfiedCount = 0;

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Feedback fb = doc.toObject(Feedback.class);
                            if (fb != null) {
                                mList.add(fb);
                                total++;

                                switch (fb.getSentiment()) {
                                    case "Tích cực": positive++; break;
                                    case "Trung lập": neutral++; break;
                                    case "Tiêu cực": negative++; break;
                                }
                                if (fb.getRating() >= 4) satisfiedCount++;
                            }
                        }

                        adapter.notifyDataSetChanged();
                        tvTotal.setText(String.valueOf(total));

                        int percent = (total > 0) ? (satisfiedCount * 100 / total) : 0;
                        tvRate.setText(percent + "%");

                        updatePieChart(positive, neutral, negative);
                        updateLineChart();

                        if (total == 0) createSampleData();
                    }
                });
    }

    private void updatePieChart(int pos, int neu, int neg) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (pos > 0) entries.add(new PieEntry(pos, "Tích cực"));
        if (neu > 0) entries.add(new PieEntry(neu, "Trung lập"));
        if (neg > 0) entries.add(new PieEntry(neg, "Tiêu cực"));

        if (entries.isEmpty()) {
            pieChart.setCenterText("Chưa có\ndữ liệu");
            pieChart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#FFC107"), Color.parseColor("#F44336"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setCenterText("Cảm xúc");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void updateLineChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 10));
        entries.add(new Entry(1, 25));
        entries.add(new Entry(2, 18));
        entries.add(new Entry(3, 35));

        LineDataSet dataSet = new LineDataSet(entries, "Xu hướng Tích cực");
        dataSet.setColor(Color.parseColor("#2979FF"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#2979FF"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BBDEFB"));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4"}));
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void createSampleData() {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        long now = System.currentTimeMillis();
        db.collection("Feedbacks").document("SAMPLE1").set(new Feedback("SAMPLE1", "Tour Đà Nẵng", "Tuyệt vời, hướng dẫn viên nhiệt tình!", "Tích cực", 5, date, now));
        db.collection("Feedbacks").document("SAMPLE2").set(new Feedback("SAMPLE2", "Tour Hạ Long", "Đồ ăn hơi nguội, cần cải thiện.", "Trung lập", 3, date, now-1000));
    }

    private void showAddFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        EditText etTour = new EditText(getContext()); etTour.setHint("Tên Tour");
        EditText etContent = new EditText(getContext()); etContent.setHint("Nội dung nhận xét");
        EditText etRating = new EditText(getContext()); etRating.setHint("Số sao (1-5)");
        etRating.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        Spinner spSentiment = new Spinner(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Tích cực", "Trung lập", "Tiêu cực"});
        spSentiment.setAdapter(adapter);

        layout.addView(etTour);
        layout.addView(etContent);
        layout.addView(etRating);
        layout.addView(spSentiment);

        builder.setView(layout);
        builder.setTitle("Thêm Đánh giá Mới");
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String tour = etTour.getText().toString();
            String content = etContent.getText().toString();
            String sentiment = spSentiment.getSelectedItem().toString();
            int rating = 5;
            try { rating = Integer.parseInt(etRating.getText().toString()); } catch (Exception e){}

            String id = "FB-" + System.currentTimeMillis();
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            Feedback fb = new Feedback(id, tour, content, sentiment, rating, date, System.currentTimeMillis());

            db.collection("Feedbacks").document(id).set(fb)
                    .addOnSuccessListener(v -> Toast.makeText(getContext(), "Đã thêm đánh giá!", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}