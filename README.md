# jmcpx

jmcpx is a Command-Line Interface (CLI) client designed for interacting with MCP (Modular Command Processor) servers. It provides a robust set of tools to manage and interact with MCP servers while seamlessly integrating with various Large Language Model (LLM) configurations.

## Features

- **Session Management**: Start and manage interactive sessions with MCP servers.
- **Tool and Resource Listing**: Retrieve and display available tools and resources provided by MCP servers.
- **LLM Integration**: Supports multiple LLM configurations, including OpenAI, Anthropic, and Bedrock.
- **Markdown Rendering**: Outputs responses in Markdown format for improved readability.
- **Customizable Configurations**: Easily configure MCP and LLM settings using JSON and TOML files.

## Requirements

- **Java**: Version 21 or higher
- **Maven**: For dependency management and building the project

## Installation

1. Clone the repository:
   ```sh
   git clone ...
   cd jmcpx

2. Build the project using Maven:
   ```sh
   mvn clean package
   ```

3. Run the application:
   ```sh
   java -jar target/jmcpx-1.0-SNAPSHOT.jar
   ```

## Usage

The application provides several commands to interact with MCP servers and LLM configurations. Below are the available commands:

### Start a Session

Start an interactive session with an MCP server:

```sh
java -jar target/jmcpx-1.0-SNAPSHOT.jar session
```
Options:

- `-c` or `--mcp`: Specify the location of the `mcp.json` file (default: `mcp.json`).
- `-l` or `--llm`: Specify the location of the `llm.toml` file (default: `llm.toml`).

### List Details

List available tools and resources from MCP servers:
```sh
java -jar target/jmcpx-1.0-SNAPSHOT.jar list
```
Options:
- `-c` or `--mcp`: Specify the location of the `mcp.json` file (default: `mcp.json`).
- `-l` or `--llm`: Specify the location of the `llm.toml` file (default: `llm.toml`).

## Configuration

### MCP Configuration
The `mcp.json` file defines the MCP servers and their configurations. Example:
```json
{
  "mcpServers": {
    "server1": {
      "command": "path/to/server1",
      "args": ["--arg1", "--arg2"],
      "env": {
        "ENV_VAR1": "value1"
      }
    }
  }
}
```

### LLM Configuration
The `llm.toml` file defines the LLM configurations. Example:
```toml
[openai]
modelName = "gpt-4"
apiKey = "your-openai-api-key"
default = true

[anthropic]
modelName = "claude-v1"
apiKey = "your-anthropic-api-key"
default = false
```

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.