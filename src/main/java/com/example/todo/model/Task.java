package com.example.todo.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name="tasks")
public class Task {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=255) private String tache;
    @Column(nullable=false) private LocalDate date;
    protected Task() {}
    public Task(String tache, LocalDate date) { this.tache=tache; this.date=date; }
    public Long getId() { return id; }
    public String getTache() { return tache; }
    public LocalDate getDate() { return date; }
    public void setTache(String tache) { this.tache=tache; }
    public void setDate(LocalDate date) { this.date=date; }
}
