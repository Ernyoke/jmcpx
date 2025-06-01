package dev.ervinszilagyi.ai.memory;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier for the memory id required by the implementations of {@link dev.langchain4j.memory.ChatMemory}
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface MemoryId {
}
