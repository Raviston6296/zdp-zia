package com.zia.agents.general;

import com.zia.agents.State;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

public class ClarificationAgent implements NodeAction<State> {

    interface Service {
        @SystemMessage("""
            You are a clarification agent. Your role is to:
            1. Analyze user requests for ambiguity or missing information
            2. Ask clarifying questions when needed
            3. Route to the appropriate agent once you have sufficient information
            
            When you need more information, ask specific questions.
            When the request is clear, provide the classification: 'import', 'export', or 'general'.
            """)
        String clarify(@dev.langchain4j.service.UserMessage String query);
    }

    private final Service service;

    public ClarificationAgent(ChatModel model) {
        this.service = AiServices.builder(Service.class)
                .chatModel(model)
                .build();
    }

    @Override
    public Map<String, Object> apply(State state) throws Exception {
        var message = state.lastMessage().orElseThrow();
        var text = switch (message.type()) {
            case USER -> ((UserMessage) message).singleText();
            case AI -> ((AiMessage) message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type());
        };

        var result = service.clarify(text);
        return Map.of("messages", AiMessage.from(result));
    }
}
