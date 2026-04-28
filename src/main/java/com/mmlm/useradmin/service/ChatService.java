package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.AuthContext;
import com.mmlm.useradmin.dto.chat.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatService {

    public ChatResponse answer(String message, String model) {
        ChatResponse response = new ChatResponse();
        String username = AuthContext.getUsername() == null ? "当前用户" : AuthContext.getUsername();
        String modelName = (model == null || model.trim().isEmpty()) ? "规则答疑" : model.trim();

        response.setAnswer("这是一个预置的大模型占位回复，后续你只需要把后端的 mock 逻辑替换成真实模型接口调用即可。\n\n"
                + "当前模型：" + modelName + "\n"
                + "当前提问人：" + username + "\n"
                + "收到的问题：" + message + "\n\n"
                + "建议后续对接时保留这个接口结构，把 `answer` 换成真实模型返回内容即可。");
        response.setTime(LocalDateTime.now());
        return response;
    }
}
