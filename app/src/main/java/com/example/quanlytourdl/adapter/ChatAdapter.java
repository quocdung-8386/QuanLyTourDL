package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        if (msg.isUser()) {
            holder.textUser.setText(msg.getMessage());
            holder.textUser.setVisibility(View.VISIBLE);
            holder.textBot.setVisibility(View.GONE);
        } else {
            holder.textBot.setText(msg.getMessage());
            holder.textBot.setVisibility(View.VISIBLE);
            holder.textUser.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textUser, textBot;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textUser = itemView.findViewById(R.id.text_message_user);
            textBot = itemView.findViewById(R.id.text_message_bot);
        }
    }
}