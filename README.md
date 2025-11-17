# ZDP Zia - LangGraph4j Hello World

A Maven project demonstrating the use of LangGraph4j with Java 15.

## Project Information

- **Language**: Java 15
- **Build Tool**: Maven
- **Framework**: LangGraph4j (v1.0.0-beta1)

## Project Structure

```
zdp-zia/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── HelloWorld.java
```

## Description

This project demonstrates a simple "Hello World" application using LangGraph4j, which is a Java implementation of the LangGraph framework for building stateful, multi-actor applications with graphs.

The application creates a simple workflow graph with three nodes:
1. **greet**: Creates an initial greeting message
2. **process**: Enhances the message with an emoji
3. **display**: Displays the final message

## Prerequisites

- Java 15 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean install
```

## Running the Application

You can run the application using Maven:

```bash
mvn exec:java
```

Or compile and run directly:

```bash
mvn clean package
java -cp target/zdp-zia-1.0-SNAPSHOT.jar com.example.HelloWorld
```

## Expected Output

```
=== LangGraph4j Hello World ===

Executing graph...

Node 'greet' executed!
Node 'process' executed!
Node 'display' executed!

Final Message: Hello World from LangGraph4j! 🚀

=== Graph Execution Complete ===
Result: Hello World from LangGraph4j! 🚀
```

## Dependencies

- **LangGraph4j Core**: State graph implementation
- **SLF4J API**: Logging facade
- **SLF4J Simple**: Simple logging implementation

## Learn More

- [LangGraph4j GitHub](https://github.com/bsorrentino/langgraph4j)
- [LangGraph Documentation](https://langchain-ai.github.io/langgraph/)

