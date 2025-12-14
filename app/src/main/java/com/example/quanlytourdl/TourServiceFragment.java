package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import com.example.quanlytourdl.R;

public class TourServiceFragment extends Fragment {

    private static final String TAG = "TourServiceFragment";
    // Keys để nhận dữ liệu từ Fragment cha
    private static final String ARG_INCLUDED_SERVICES = "included_services";
    private static final String ARG_EXCLUDED_SERVICES = "excluded_services";

    private String includedServices;
    private String excludedServices;

    private TextView tvIncludedServices;
    private TextView tvExcludedServices;
    private TextView tvSectionExcluded; // Giả định có TextView tiêu đề cho phần không bao gồm

    // ⭐ Phương thức New Instance để nhận dữ liệu dịch vụ
    public static TourServiceFragment newInstance(String includedServices, String excludedServices) {
        TourServiceFragment fragment = new TourServiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INCLUDED_SERVICES, includedServices);
        args.putString(ARG_EXCLUDED_SERVICES, excludedServices);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            includedServices = getArguments().getString(ARG_INCLUDED_SERVICES);
            excludedServices = getArguments().getString(ARG_EXCLUDED_SERVICES);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả định layout file: fragment_tour_service.xml
        View view = inflater.inflate(R.layout.fragment_tour_service, container, false);

        // Giả định layout có hai TextView để hiển thị dịch vụ bao gồm và không bao gồm
        tvIncludedServices = view.findViewById(R.id.tv_included_services);
        tvExcludedServices = view.findViewById(R.id.tv_excluded_services);
        tvSectionExcluded = view.findViewById(R.id.tv_section_excluded);

        // ⭐ Hiển thị dữ liệu dịch vụ đã nhận từ TourDetailFragment
        if (includedServices != null && !includedServices.trim().isEmpty()) {
            tvIncludedServices.setText(includedServices);
        } else {
            tvIncludedServices.setText("Không có thông tin chi tiết về dịch vụ bao gồm.");
            Log.w(TAG, "Dịch vụ bao gồm rỗng.");
        }

        // Hiển thị dịch vụ không bao gồm (có thể ẩn nếu rỗng)
        if (excludedServices != null && !excludedServices.trim().isEmpty()) {
            tvExcludedServices.setText(excludedServices);
            tvSectionExcluded.setVisibility(View.VISIBLE);
        } else {
            tvExcludedServices.setVisibility(View.GONE);
            if (tvSectionExcluded != null) {
                tvSectionExcluded.setVisibility(View.GONE);
            }
        }

        return view;
    }
}