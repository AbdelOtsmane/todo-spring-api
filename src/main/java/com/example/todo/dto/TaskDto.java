package com.example.todo.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record TaskDto(@NotBlank @Size(max=255) String tache, @NotNull LocalDate date) {}
