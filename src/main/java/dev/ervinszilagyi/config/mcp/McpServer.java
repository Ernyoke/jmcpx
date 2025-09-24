package dev.ervinszilagyi.config.mcp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "transport"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StdioMcpServer.class, name = "stdio"),
        @JsonSubTypes.Type(value = StreamableHttpMcpServer.class, name = "http")
})
public interface McpServer {
    enum Transport {
        STDIO, HTTP
    }
}
