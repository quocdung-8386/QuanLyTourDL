package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
// Giả định R là file resource ID của Android

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaoHopDongFragment extends Fragment {

    private static final String TAG = "TaoHopDongFragment";

    /**
     * Lớp mô hình dữ liệu NCC đơn giản cho Dropdown
     */
    private static class Supplier {
        String id;
        String tenNhaCungCap;

        public Supplier(String id, String tenNhaCungCap) {
            this.id = id;
            this.tenNhaCungCap = tenNhaCungCap;
        }

        @NonNull
        @Override
        public String toString() {
            // Chuỗi sẽ hiển thị trong dropdown
            return tenNhaCungCap;
        }
    }

    // View components
    private ImageView iconBack;
    private AutoCompleteTextView edtSupplierName;
    private EditText edtContractCode;
    private EditText edtSignDate;
    private EditText edtExpiryDate;
    private EditText edtContractContent;
    private EditText edtPaymentTerms;
    private EditText edtServiceTerms;
    private Button btnCancelContract;
    private Button btnCreateNewContract;

    // Firebase
    private FirebaseFirestore db;

    // Dữ liệu NCC đã chọn
    private String selectedSupplierId; // ID NCC đã chọn từ Firebase
    private String selectedSupplierName; // Tên NCC đã chọn (dùng để kiểm tra TextWatcher)

    // Danh sách NCC để điền vào dropdown
    private List<Supplier> allSuppliersList = new ArrayList<>();

    // Giả định R.layout.fragment_tao_hop_dong tồn tại
    private final int LAYOUT_ID = R.layout.fragment_tao_hop_dong; // Placeholder cho ID layout

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Sử dụng một placeholder ID cho layout vì không có file R thực
        // Trong dự án thực tế, bạn sẽ dùng R.layout.fragment_tao_hop_dong
        // Giả định rằng R.layout.fragment_tao_hop_dong có giá trị ID hợp lệ trong dự án của bạn
        int layoutId = LAYOUT_ID;

        // Nếu bạn muốn giữ lại logic tìm ID động ban đầu (dù không được khuyến khích):
        // int layoutId = getResources().getIdentifier("fragment_tao_hop_dong", "layout", requireContext().getPackageName());

        // Log.d(TAG, "Layout ID: " + layoutId); // Debugging

        // Thay vì kiểm tra layoutId == 0, ta giả định layout được tìm thấy
        // hoặc sử dụng ID placeholder nếu không muốn phụ thuộc vào R.
        // Để code sạch, ta sử dụng phương pháp inflate chuẩn và giả định layout ID tồn tại.
        View view = inflater.inflate(layoutId, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupDatePickers();
        // Thiết lập dropdown NCC, tải dữ liệu và thêm listener kiểm tra
        setupSupplierDropdown();
        setupActionButtons();
    }

    /**
     * Khởi tạo các View
     */
    private void initViews(View view) {
        // Giả định các ID sau đây tồn tại trong layout
        iconBack = view.findViewById(R.id.icon_back);
        edtSupplierName = (AutoCompleteTextView) view.findViewById(R.id.edt_supplier_name);
        edtContractCode = view.findViewById(R.id.edt_contract_code);
        edtSignDate = view.findViewById(R.id.edt_sign_date);
        edtExpiryDate = view.findViewById(R.id.edt_expiry_date);
        edtContractContent = view.findViewById(R.id.edt_contract_content);
        edtPaymentTerms = view.findViewById(R.id.edt_payment_terms);
        edtServiceTerms = view.findViewById(R.id.edt_service_terms);
        btnCancelContract = view.findViewById(R.id.btn_cancel_contract);
        btnCreateNewContract = view.findViewById(R.id.btn_create_new_contract);
    }

    private void setupToolbar() {
        // Xử lý sự kiện nhấn icon Quay lại
        iconBack.setOnClickListener(v -> closeFragment());
    }

    /**
     * Tải danh sách NCC, thiết lập dropdown và listener.
     */
    private void setupSupplierDropdown() {
        // 1. Tải dữ liệu NCC từ Firebase
        loadSuppliersForDropdown();

        // 2. Thiết lập Listener khi một item được chọn
        edtSupplierName.setOnItemClickListener((parent, view, position, id) -> {
            Supplier selected = (Supplier) parent.getItemAtPosition(position);
            selectedSupplierId = selected.id;
            selectedSupplierName = selected.tenNhaCungCap; // Lưu tên NCC đã chọn
            Toast.makeText(getContext(), "Đã chọn NCC: " + selectedSupplierName, Toast.LENGTH_SHORT).show();
            // Đảm bảo TextWatcher không kích hoạt khi set text từ item click
            edtSupplierName.removeTextChangedListener(supplierTextWatcher);
            edtSupplierName.setText(selectedSupplierName, false); // false = không hiển thị dropdown
            edtSupplierName.addTextChangedListener(supplierTextWatcher);
        });

        // 3. Thiết lập TextWatcher để kiểm tra nếu người dùng thay đổi thủ công
        edtSupplierName.addTextChangedListener(supplierTextWatcher);

        // Cần thiết để hiển thị dropdown khi click vào
        edtSupplierName.setOnClickListener(v -> edtSupplierName.showDropDown());
    }

    /**
     * TextWatcher để đảm bảo người dùng chỉ sử dụng giá trị từ dropdown.
     */
    private final TextWatcher supplierTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Không làm gì
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Không làm gì
        }

        @Override
        public void afterTextChanged(Editable s) {
            String currentText = s.toString();
            // Nếu người dùng đã chọn một NCC VÀ text hiện tại không khớp với tên đã chọn
            if (selectedSupplierId != null && !currentText.equals(selectedSupplierName)) {
                // Điều này có nghĩa là người dùng đã sửa thủ công, cần xóa ID đã chọn
                selectedSupplierId = null;
                selectedSupplierName = null;
                Log.d(TAG, "ID NCC bị xóa do sửa thủ công.");
            }
        }
    };

    /**
     * Tải danh sách Nhà Cung Cấp từ Firebase Firestore
     */
    private void loadSuppliersForDropdown() {
        // Đường dẫn collection có thể cần điều chỉnh theo quy tắc bảo mật của bạn
        db.collection("NhaCungCap")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allSuppliersList.clear();
                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        String id = document.getId();
                        String name = document.getString("tenNhaCungCap");

                        if (name != null) {
                            allSuppliersList.add(new Supplier(id, name));
                        }
                    });

                    // Tạo ArrayAdapter và gán vào AutoCompleteTextView
                    ArrayAdapter<Supplier> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line, // Layout cơ bản cho dropdown item
                            allSuppliersList
                    );
                    edtSupplierName.setAdapter(adapter);

                    if (allSuppliersList.isEmpty()) {
                        Log.w(TAG, "Không có NCC nào được tải.");
                        Toast.makeText(getContext(), "Không tìm thấy Nhà Cung Cấp nào.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải danh sách NCC", e);
                    Toast.makeText(getContext(), "Lỗi kết nối Firebase khi tải NCC.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupDatePickers() {
        // Ngăn gõ tay và chỉ cho phép chọn bằng Date Picker
        edtSignDate.setFocusable(false);
        edtSignDate.setOnClickListener(v -> showDatePicker(edtSignDate));

        edtExpiryDate.setFocusable(false);
        edtExpiryDate.setOnClickListener(v -> showDatePicker(edtExpiryDate));
    }

    private void showDatePicker(final EditText dateField) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(y, m, d);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateField.setText(sdf.format(selectedDate.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    private void setupActionButtons() {
        // Nút Hủy
        btnCancelContract.setOnClickListener(v -> closeFragment());

        // Nút Tạo mới
        btnCreateNewContract.setOnClickListener(v -> createNewContract());
    }

    private boolean validateForm() {
        // Kiểm tra xem NCC đã được chọn HỢP LỆ (qua ID) hay chưa
        if (selectedSupplierId == null || selectedSupplierId.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn Nhà Cung Cấp TỪ DANH SÁCH trước khi tạo hợp đồng.", Toast.LENGTH_LONG).show();
            edtSupplierName.setError("Cần chọn từ danh sách");
            return false;
        }

        String contractCode = edtContractCode.getText().toString().trim();
        String signDate = edtSignDate.getText().toString().trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();

        if (contractCode.isEmpty() || signDate.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đủ Mã HĐ, Ngày ký và Ngày hết hạn.", Toast.LENGTH_LONG).show();
            if (contractCode.isEmpty()) edtContractCode.setError("Không được để trống");
            if (signDate.isEmpty()) edtSignDate.setError("Không được để trống");
            if (expiryDate.isEmpty()) edtExpiryDate.setError("Không được để trống");
            return false;
        }

        // Thêm các logic kiểm tra ngày tháng, định dạng mã hợp đồng tại đây
        return true;
    }

    /**
     * Thu thập dữ liệu và lưu Hợp đồng mới lên Firestore
     */
    private void createNewContract() {
        if (!validateForm()) {
            return;
        }

        String contractCode = edtContractCode.getText().toString().trim();
        String signDate = edtSignDate.getText().toString().trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();
        String contractContent = edtContractContent.getText().toString().trim();
        String paymentTerms = edtPaymentTerms.getText().toString().trim();
        String serviceTerms = edtServiceTerms.getText().toString().trim();

        // Tạo Map dữ liệu cho Firestore
        Map<String, Object> contractData = new HashMap<>();
        contractData.put("maHopDong", contractCode);
        contractData.put("supplierId", selectedSupplierId); // ID NCC
        contractData.put("tenNhaCungCap", selectedSupplierName); // Tên NCC (để tiện cho việc hiển thị)
        contractData.put("ngayKy", signDate);
        contractData.put("ngayHetHan", expiryDate);
        contractData.put("noiDung", contractContent);
        contractData.put("dieuKhoanThanhToan", paymentTerms);
        contractData.put("dieuKhoanDichVu", serviceTerms);
        contractData.put("trangThai", "Đang hiệu lực");
        contractData.put("createdAt", FieldValue.serverTimestamp());

        // Lưu vào collection "HopDong"
        db.collection("HopDong")
                .add(contractData)
                .addOnSuccessListener(documentReference -> {
                    updateSupplierContractStatus(selectedSupplierId, contractCode);

                    Toast.makeText(getContext(), "Tạo hợp đồng mới thành công! Mã: " + contractCode, Toast.LENGTH_LONG).show();
                    closeFragment();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tạo hợp đồng mới", e);
                    Toast.makeText(getContext(), "Lỗi khi tạo hợp đồng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Cập nhật thông tin hợp đồng active mới nhất vào document NhaCungCap
     */
    private void updateSupplierContractStatus(String supplierId, String contractCode) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("maHopDongGanNhat", contractCode); // Đổi tên field thành "maHopDongGanNhat" để rõ ràng hơn
        updates.put("trangThaiHopDong", "Active");

        // Sử dụng set() với merge = true để không ghi đè toàn bộ document NCC
        db.collection("NhaCungCap").document(supplierId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cập nhật trạng thái NCC thành công."))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi cập nhật trạng thái NCC: " + e.getMessage()));
    }

    /**
     * Đóng Fragment (Ví dụ: quay lại màn hình danh sách)
     */
    private void closeFragment() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }
}