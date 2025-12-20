package com.example.quanlytourdl;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.adapter.ChatAdapter;
import com.example.quanlytourdl.model.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText editMessage;
    private ImageButton btnSend;
    private ImageView btnBack;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Ánh xạ View
        recyclerChat = findViewById(R.id.recyclerChat);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        // Cấu hình RecyclerView
        messageList = new ArrayList<>();
        // Thêm tin nhắn chào mừng
        messageList.add(new ChatMessage("Xin chào! Tôi có thể giúp gì cho việc quản lý tour của bạn?", false));

        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        // Xử lý sự kiện nút Gửi
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendMessage() {
        String content = editMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        // 1. Hiển thị tin nhắn của User
        messageList.add(new ChatMessage(content, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerChat.scrollToPosition(messageList.size() - 1);
        editMessage.setText("");

        // 2. Giả lập Bot trả lời (Sau này bạn sẽ thay bằng gọi API AI thật)
        handleBotResponse(content);
    }

    private void handleBotResponse(String userQuery) {
        // Giả lập độ trễ 1 giây để giống thật
        recyclerChat.postDelayed(() -> {
            String botReply = "Tôi đã nhận được câu hỏi về: " + userQuery + ". Tính năng AI đang được phát triển!";
            
            // Logic if-else đơn giản cho demo
            if (userQuery.toLowerCase().contains("doanh thu")) {
                botReply = "Doanh thu tháng này đang tăng trưởng tốt. Bạn có thể xem chi tiết ở Tab Thống kê.";
            } else if (userQuery.toLowerCase().contains("tour")) {
                botReply = "Hiện tại có 5 tour đang hoạt động và 2 tour đang chờ duyệt.";
            }

            messageList.add(new ChatMessage(botReply, false));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerChat.scrollToPosition(messageList.size() - 1);
        }, 1000);
    }
}