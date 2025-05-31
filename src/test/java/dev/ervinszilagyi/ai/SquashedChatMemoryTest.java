package dev.ervinszilagyi.ai;

import dev.ervinszilagyi.ai.memory.SquashedChatMemory;
import dev.ervinszilagyi.ai.memory.SquashedChatMemoryStore;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class SquashedChatMemoryTest {
    private SquashedChatMemoryStore squashedChatMemoryStore;
    private SquashedChatMemory squashedChatMemory;

    @BeforeEach
    public void setUp() {
        squashedChatMemoryStore = Mockito.mock(SquashedChatMemoryStore.class);
        squashedChatMemory = new SquashedChatMemory("id", squashedChatMemoryStore);
    }

    @Test
    @DisplayName("Test squashing tool execution messages. Remove in-betwwen AI and ToolExecutionResult messages.")
    public void testSquashToolExecutionMessages() {
        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder().id("tool-execution-id").build();
        List<ChatMessage> messages = List.of(
                SystemMessage.systemMessage("This is a system message"),
                UserMessage.userMessage("This is an user message"),
                AiMessage.aiMessage(List.of(ToolExecutionRequest.builder().build())),
                ToolExecutionResultMessage.toolExecutionResultMessage(toolExecutionRequest, "result"),
                AiMessage.aiMessage("This is a summary of the result")
        );

        List<ChatMessage> expectedMessages = List.of(
                SystemMessage.systemMessage("This is a system message"),
                UserMessage.userMessage("This is an user message"),
                AiMessage.aiMessage("This is a summary of the result")
        );

        Mockito.when(squashedChatMemoryStore.getMessages(Mockito.any())).thenReturn(messages);

        squashedChatMemory.squashToolExecutions();
        Mockito.verify(squashedChatMemoryStore, Mockito.times(1)).getMessages(Mockito.any());
        Mockito.verify(squashedChatMemoryStore, Mockito.times(1))
                .updateMessages(Mockito.any(), Mockito.eq(expectedMessages));
    }
}
