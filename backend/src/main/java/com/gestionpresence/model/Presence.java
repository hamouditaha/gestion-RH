package com.gestionpresence.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "presences")
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    private LocalDateTime dateHeurePointage;
    private String typePointage; // "ENTREE" ou "SORTIE"
    private boolean retard;
    private int minutesRetard;
    
    // Constructeurs
    public Presence() {}
    
    public Presence(Employee employee, LocalDateTime dateHeurePointage, String typePointage) {
        this.employee = employee;
        this.dateHeurePointage = dateHeurePointage;
        this.typePointage = typePointage;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public LocalDateTime getDateHeurePointage() { return dateHeurePointage; }
    public void setDateHeurePointage(LocalDateTime dateHeurePointage) { this.dateHeurePointage = dateHeurePointage; }
    
    public String getTypePointage() { return typePointage; }
    public void setTypePointage(String typePointage) { this.typePointage = typePointage; }
    
    public boolean isRetard() { return retard; }
    public void setRetard(boolean retard) { this.retard = retard; }
    
    public int getMinutesRetard() { return minutesRetard; }
    public void setMinutesRetard(int minutesRetard) { this.minutesRetard = minutesRetard; }
}