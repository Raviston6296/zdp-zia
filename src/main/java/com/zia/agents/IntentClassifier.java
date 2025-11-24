package com.zia.agents;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import dev.langchain4j.service.V;
import dev.langchain4j.service.SystemMessage;

import java.util.Map;

import static java.lang.String.format;

public class IntentClassifier implements NodeAction<State> {

    static class Router {
        @Description("Worker to route to next. If no workers needed, route to FINISH.")
        String next;

        @Override
        public String toString() {
            return format( "Router[next: %s]",next);
        }
    }

    interface Service {
        @SystemMessage("You are the INTENT CLASSIFIER AGENT in the Zoho dataprep multi-agent system.\n" +
                "You are the ONLY agent allowed to interpret the user's message and decide \n" +
                "WHICH Worker Agent should handle it next.\n" +
                "\n" +
                "IMPORTANT:\n" +
                "- You DO NOT extract parameters.\n" +
                "- You DO NOT generate function_call JSON.\n" +
                "- You DO NOT infer missing fields.\n" +
                "- You DO NOT execute tasks.\n" +
                "- Worker Agents (ImportAgent, TransformAgent, JoinAgent, etc.) will do all that.\n" +
                "\n" +
                "Your ONLY responsibilities:\n" +
                "1. Understand the user's natural language input.\n" +
                "2. Identify the high-level intent category.\n" +
                "3. Route the message to the correct Worker Agent.\n" +
                "\n" +
                "======================================================\n" +
                "INTENT CATEGORIES (decide one)\n" +
                "======================================================\n" +
                "\n" +
                "- \"importer\"" +
                "- \"transformer\"" +
                "- \"exporter\"" +
                "\n" +
                "Examples:\n" +
                "- “load customers from s3”                → importer\n" +
                "- “clean the phone column”               → transformer\n" +
                "- “send output to snowflake”             → exporter\n" +
                "\n" +
                "======================================================\n" +
                "OUTPUT FORMAT\n" +
                "======================================================\n" +
                "\n" +
                "\n" +
                "importer" +
                "transformer" +
                "ExportAgent" +
                "FINISH" +
                "\n" +
                "WorkerAgentName must be one of:\n" +
                "- importer\n" +
                "- transformer\n" +
                "- ExportAgent\n" +
                "\n" +
                "======================================================\n" +
                "RULES\n" +
                "======================================================\n" +
                "\n" +
                "- DO NOT produce function_call JSON.\n" +
                "- DO NOT produce tool names.\n" +
                "- DO NOT generate or validate parameters.\n" +
                "- DO NOT plan multi-step workflows.\n" +
                "- DO NOT skip returning the raw user message.\n" +
                "- ONLY classify and route.\n" +
                "\n" +
                "Be deterministic, precise, and minimal.\n.When finished, respond with FINISH.")

        Router evaluate(@V("members") String members, @dev.langchain4j.service.UserMessage String userMessage);
    }

    final Service service;
    public final String[] members = {"importer", "transformer" ,"exporter"};

    public IntentClassifier(ChatModel model ) {
        service = AiServices.create( Service.class, model );
    }

    @Override
    public Map<String, Object> apply(State state) throws Exception {

        var message = state.lastMessage().orElseThrow();

        var text = switch( message.type() ) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type() );
        };

        var m = String.join(",", members);

        var result = service.evaluate( m, text );

        return Map.of( "next", result.next );
    }
}
