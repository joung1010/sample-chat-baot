package com.app.chatboat.dto;


public record ChatMessage(String role, String content) {
    

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }
    
    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }
    
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }
    

    public boolean isUser() {
        return "user".equals(role);
    }
    
    public boolean isAssistant() {
        return "assistant".equals(role);
    }
    
    public boolean isSystem() {
        return "system".equals(role);
    }
}
