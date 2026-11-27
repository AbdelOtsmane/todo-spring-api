package com.example.todo.controller;
import com.example.todo.dto.TaskDto;
import com.example.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService service;
    public TaskController(TaskService service) { this.service=service; }
    @GetMapping public List<TaskDto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public TaskDto findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public ResponseEntity<TaskDto> create(@Valid @RequestBody TaskDto dto) { var created=service.create(dto); return ResponseEntity.created(URI.create("/api/tasks/"+created.id())).body(created.task()); }
    @PutMapping("/{id}") public TaskDto update(@PathVariable Long id,@Valid @RequestBody TaskDto dto) { return service.update(id,dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
