package com.zia.agents;

import com.zia.agents.general.ClarificationAgent;
import com.zia.agents.importer.ImporterAgent;
import com.zia.agents.transformer.TransformerAgent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;
import java.util.Scanner;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class ZDPZia {

    public static void main(String[] args) throws GraphStateException {
        var model = OllamaChatModel.builder()
                .baseUrl( "http://localhost:11434" )
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("qwen2.5:7b")
                .build();

        var modelWithTool = OllamaChatModel.builder()
                .baseUrl( "http://localhost:11434" )
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("qwen2.5:7b")
                .build();

        var intentClassifier = new IntentClassifier(model);
        var importer = new ImporterAgent(modelWithTool);
        var transformer = new TransformerAgent(modelWithTool);
        var clarification = new ClarificationAgent(modelWithTool);

        var workflow = new StateGraph<>( State.SCHEMA, new StateSerializer() )
                .addNode( "intentClassifier", node_async(intentClassifier))
                .addNode( "importer", node_async(importer) )
                .addNode( "transformer", node_async(transformer) )
                .addEdge( START, "intentClassifier")
                .addConditionalEdges( "intentClassifier",
                        edge_async( state ->
                                state.next().orElseThrow()
                        ), Map.of(
                                "FINISH", END,
                                "importer", "importer",
                                "transformer", "transformer"
                        ))
                .addEdge( "importer", "intentClassifier")
                .addEdge( "transformer", "intentClassifier");

        var graph = workflow.compile();

        // Interactive loop
        Scanner scanner = new Scanner(System.in);
        Map<String, Object> currentState = Map.of("messages", UserMessage.from("want to import data bro"));

        while (true) {
            for (var event : graph.stream(currentState)) {
                System.out.println(event.toString());

                // Check if we need user input
                State state = (State) event.state();
                if (state.next().orElse("").equals("waitForInput")) {
                    System.out.print("\nYour response: ");
                    String userInput = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(userInput)) {
                        scanner.close();
                        return;
                    }

                    // Continue with user's input
                    currentState = Map.of("messages", UserMessage.from(userInput));
                    break;
                }

                // If finished, exit
                if (state.next().orElse("").equals("FINISH")) {
                    scanner.close();
                    return;
                }
            }
        }
    }

}
