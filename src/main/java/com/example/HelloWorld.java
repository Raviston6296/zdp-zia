package com.example;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;

/**
 * Hello World application using LangGraph4j with Java 17
 */
public class HelloWorld {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LangGraph4j Hello World (Java 17) ===\n");

        // Create a state graph using AgentState
        StateGraph<AgentState> workflow = new StateGraph<>(AgentState::new);

        // Add a node that creates a greeting
        workflow.addNode("greet", (AsyncNodeAction<AgentState>) state ->
            CompletableFuture.supplyAsync(() -> {
                System.out.println("Node 'greet' executed!");
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Hello World from LangGraph4j!");
                return result;
            })
        );

        // Add a node that processes the greeting
        workflow.addNode("process", (AsyncNodeAction<AgentState>) state ->
            CompletableFuture.supplyAsync(() -> {
                System.out.println("Node 'process' executed!");
                String message = (String) state.data().get("message");
                Map<String, Object> result = new HashMap<>();
                result.put("message", message + " 🚀");
                return result;
            })
        );

        // Add a node that displays the final message
        workflow.addNode("display", (AsyncNodeAction<AgentState>) state ->
            CompletableFuture.supplyAsync(() -> {
                System.out.println("Node 'display' executed!");
                String message = (String) state.data().get("message");
                System.out.println("\nFinal Message: " + message);
                Map<String, Object> result = new HashMap<>();
                result.put("message", message);
                return result;
            })
        );

        // Define the flow
        workflow.addEdge(StateGraph.START, "greet");
        workflow.addEdge("greet", "process");
        workflow.addEdge("process", "display");
        workflow.addEdge("display", END);

        // Compile the graph
        CompiledGraph<AgentState> app = workflow.compile();

        // Create initial state
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("message", "");
        AgentState initialState = new AgentState(initialData);

        // Execute the graph
        System.out.println("Executing graph...\n");
    }
}

