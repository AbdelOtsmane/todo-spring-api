package com.example.todo.service;
import com.example.todo.dto.TaskDto;
import java.util.List;
public interface TaskService {
    record Created(Long id, TaskDto task) {}
    List<TaskDto> findAll();
    TaskDto findById(Long id);
    Created create(TaskDto dto);
    TaskDto update(Long id, TaskDto dto);
    void delete(Long id);
}
