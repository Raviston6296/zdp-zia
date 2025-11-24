package com.zia.agents.importer;

import com.zia.agents.State;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import jdk.jfr.Description;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

public class ImporterAgent implements NodeAction<State> {

    static class Tools {

        @Tool("To import/pull files from Amazon S3 and create as dataset in application")
        String import_s3(
                @P("Name of the S3 bucket") String bucket_name,
                @P("Folder path where the file is located") String folder,
                @P("Name of the S3 connection") String connection_name,
                @P("Name of the file names present in S3 to be imported") String[] file_names
        ) {
            // TODO: Implement S3 import logic
            // Example implementation:
            try {
                // Validate inputs
                if (bucket_name == null || bucket_name.isEmpty()) {
                    return "Error: bucket_name is required";
                }
                if (connection_name == null || connection_name.isEmpty()) {
                    return "Error: connection_name is required";
                }
                if (file_names == null || file_names.length == 0) {
                    return "Error: at least one file_name is required";
                }

                // Your S3 import logic here
                // Example: Use AWS SDK to download files
                String result = String.format(
                        "Successfully imported %d files from s3://%s/%s using connection '%s'",
                        file_names.length,
                        bucket_name,
                        folder != null ? folder : "",
                        connection_name
                );

                return result;
            } catch (Exception e) {
                return "Error importing from S3: " + e.getMessage();
            }
        }
    }

    interface Service {
        @SystemMessage("""
                You are IMPORTAGENT in the ZDP-ZIA multi-agent system.
                
                You are a Worker LLM Agent who handles ONLY data-ingestion (import) related
                tasks that the Smart Agent routes to you.
                
                You DO NOT:
                - plan workflows
                - route to other agents
                - create multi-step pipelines
                - infer join keys or transformations
                - execute tools
                - return natural language in function_call responses
                
                You ONLY:
                - understand the user’s import request
                - extract required parameters
                - validate them
                - if something is missing → ask_user
                - if all parameters are ready → return function_call
                
                Do NOT include any extra text outside these JSON structures.
                
                ======================================================
                RULES
                ======================================================
                
                - Ask for missing parameters instead of guessing them.
                - Never infer sensitive things like credentials.
                - Never hallucinate bucket names, table names, paths, or schema details.
                - Validate argument shapes (e.g., bucket name syntax, port must be a number).
                - Only generate function_call for ONE function per message.
                - format must match file extension if obvious (e.g., customers.csv → csv)
                - If multiple interpretations exist → ask_user
                - If user provides a URL like s3://bucket/path, split into bucket + path
                - Do not route; Smart Agent handles routing.
                - Keep all outputs deterministic and JSON-only.
                
                ======================================================
                BEHAVIOR SUMMARY
                ======================================================
                
                1. Read raw_user_input.
                2. Identify which import function to use.
                3. Extract parameters.
                4. If missing → ask_user.
                5. If complete → return function_call only.
            """)
        String importer(@dev.langchain4j.service.UserMessage  String query);
    }

    final Service service;

    public ImporterAgent( ChatModel model ) {
        service = AiServices.builder( Service.class )
                .chatModel(model)
                .tools( new Tools() )
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
        var result = service.importer(text);
        return Map.of("messages", AiMessage.from(result));

    }
}
