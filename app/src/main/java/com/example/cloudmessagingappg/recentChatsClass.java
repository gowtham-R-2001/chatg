package com.example.cloudmessagingappg;

public class recentChatsClass
{
    private String chatId,name;
    private String imageUrl, recentchat;

    public recentChatsClass(String chatId, String name, String imageUrl, String recentchat) {
        this.chatId = chatId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.recentchat = recentchat;
    }

    public recentChatsClass() {
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRecentchat() {
        return recentchat;
    }

    public void setRecentchat(String recentchat) {
        this.recentchat = recentchat;
    }
}
