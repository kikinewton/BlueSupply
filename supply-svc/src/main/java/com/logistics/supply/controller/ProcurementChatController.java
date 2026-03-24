package com.logistics.supply.controller;

import com.logistics.supply.dto.ChatRequest;
import com.logistics.supply.mcp.ProcurementMcpTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ProcurementChatController {

    private static final String SYSTEM_PROMPT = """
            You are a procurement assistant for the BlueSupply system. \
            Use the available tools to answer questions about procurement requests, LPOs, GRNs, \
            payments, supplier performance, and financial metrics. \
            Be concise, factual, and grounded in the data returned by the tools. \
            Do not speculate about data not returned by the tools.""";

    private final ChatClient chatClient;
    private final ProcurementMcpTools procurementMcpTools;

    public ProcurementChatController(ChatClient.Builder chatClientBuilder,
                                     ProcurementMcpTools procurementMcpTools) {
        this.chatClient = chatClientBuilder.build();
        this.procurementMcpTools = procurementMcpTools;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);

        chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(request.message())
                .tools(procurementMcpTools)
                .stream()
                .content()
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }
}
