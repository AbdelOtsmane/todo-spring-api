package com.example.todo.service;
import com.example.todo.dto.TaskDto;
import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class TaskServiceTest {
    private final TaskRepository repo=mock(TaskRepository.class);
    private final TaskService service=new TaskServiceImpl(repo);
    private final TaskDto dto=new TaskDto("Test",LocalDate.of(2026,11,27));
    @Test void list() { when(repo.findAll(Sort.by("id"))).thenReturn(List.of(new Task(dto.tache(),dto.date()))); assertEquals(List.of(dto),service.findAll()); }
    @Test void read() { when(repo.findById(1L)).thenReturn(Optional.of(new Task(dto.tache(),dto.date()))); assertEquals(dto,service.findById(1L)); }
    @Test void create() { Task task=mock(Task.class); when(task.getId()).thenReturn(1L); when(task.getTache()).thenReturn(dto.tache()); when(task.getDate()).thenReturn(dto.date()); when(repo.save(any(Task.class))).thenReturn(task); assertEquals(new TaskService.Created(1L,dto),service.create(dto)); }
    @Test void update() { Task task=new Task("Old",dto.date()); when(repo.findById(1L)).thenReturn(Optional.of(task)); assertEquals(dto,service.update(1L,dto)); assertEquals(dto.tache(),task.getTache()); }
    @Test void delete() { Task task=new Task(dto.tache(),dto.date()); when(repo.findById(1L)).thenReturn(Optional.of(task)); service.delete(1L); verify(repo).delete(task); }
    @Test void missing() { when(repo.findById(1L)).thenReturn(Optional.empty()); assertEquals(404,assertThrows(ResponseStatusException.class,()->service.findById(1L)).getStatusCode().value()); }
}
