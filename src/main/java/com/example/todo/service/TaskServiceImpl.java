package com.example.todo.service;
import com.example.todo.dto.TaskDto;
import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
@Transactional(readOnly=true)
public class TaskServiceImpl implements TaskService {
    private final TaskRepository repository;
    public TaskServiceImpl(TaskRepository repository) { this.repository=repository; }
    private Task require(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tache introuvable")); }
    private TaskDto toDto(Task task) { return new TaskDto(task.getTache(), task.getDate()); }
    public List<TaskDto> findAll() { return repository.findAll(Sort.by("id")).stream().map(this::toDto).toList(); }
    public TaskDto findById(Long id) { return toDto(require(id)); }
    @Transactional public Created create(TaskDto dto) { Task task=repository.save(new Task(dto.tache(),dto.date())); return new Created(task.getId(),toDto(task)); }
    @Transactional public TaskDto update(Long id, TaskDto dto) { Task task=require(id); task.setTache(dto.tache()); task.setDate(dto.date()); return toDto(task); }
    @Transactional public void delete(Long id) { repository.delete(require(id)); }
}
