package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.quanlytourdl.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.quanlytourdl.adapter.ReviewAdapter; // ⭐ Giả định bạn có ReviewAdapter
import com.example.quanlytourdl.model.Review;       // ⭐ Giả định bạn có Review Model

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TourReviewFragment extends Fragment {

    private static final String TAG = "TourReviewFragment";
    private static final String ARG_TOUR_ID = "tour_id"; // ⭐ Dùng để nhận ID Tour

    private RecyclerView reviewRecyclerView;
    private ReviewAdapter reviewAdapter;
    private FirebaseFirestore db;
    private String tourId;

    // ⭐ Phương thức New Instance để nhận ID Tour từ Fragment cha (TourDetailFragment)
    public static TourReviewFragment newInstance(String tourId) {
        TourReviewFragment fragment = new TourReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOUR_ID, tourId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            tourId = getArguments().getString(ARG_TOUR_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả định layout đơn giản cho đánh giá: fragment_tour_review.xml
        View view = inflater.inflate(R.layout.fragment_tour_review, container, false);

        reviewRecyclerView = view.findViewById(R.id.recycler_reviews);
        setupRecyclerView();

        // ⭐ Bắt đầu tải dữ liệu Firebase
        if (tourId != null) {
            loadReviews(tourId);
        } else {
            Log.e(TAG, "Thiếu Tour ID để tải đánh giá.");
            Toast.makeText(getContext(), "Không thể tải đánh giá (Thiếu ID Tour).", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void setupRecyclerView() {
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo Adapter với danh sách rỗng ban đầu
        reviewAdapter = new ReviewAdapter(requireContext(), Collections.emptyList());
        reviewRecyclerView.setAdapter(reviewAdapter);
    }

    /**
     * ⭐ Tải danh sách đánh giá từ Firestore dựa trên Tour ID.
     */
    private void loadReviews(String tourId) {
        Log.d(TAG, "Bắt đầu tải đánh giá cho Tour ID: " + tourId);

        db.collection("Reviews")
                .whereEqualTo("maTour", tourId) // Truy vấn các đánh giá có maTour khớp
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Chuyển đổi Document thành đối tượng Review
                                Review review = document.toObject(Review.class);
                                reviews.add(review);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi mapping Review từ Firestore.", e);
                            }
                        }

                        // Cập nhật Adapter
                        reviewAdapter.updateList(reviews);
                        Log.d(TAG, "Đã tải thành công " + reviews.size() + " đánh giá.");

                        if (reviews.isEmpty() && isAdded()) {
                            Toast.makeText(getContext(), "Tour này chưa có đánh giá nào.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.e(TAG, "Lỗi Firebase khi tải đánh giá.", task.getException());
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Lỗi tải đánh giá: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}