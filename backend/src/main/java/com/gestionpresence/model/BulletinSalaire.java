package com.gestionpresence.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bulletins_salaire")
public class BulletinSalaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate periodeDebut;
    private LocalDate periodeFin;

    private double salaireBase;
    private int joursTravailles;
    private int joursAbsence;
    private int totalRetardsMinutes;
    private double heuresSupplementaires;

    private double deductionAbsences;
    private double deductionRetards;
    private double salaireNet;

    private boolean envoye;

    // Constructeurs
    public BulletinSalaire() {}

    public BulletinSalaire(Employee employee, LocalDate periodeDebut, LocalDate periodeFin) {
        this.employee = employee;
        this.periodeDebut = periodeDebut;
        this.periodeFin = periodeFin;
        this.envoye = false;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getPeriodeDebut() { return periodeDebut; }
    public void setPeriodeDebut(LocalDate periodeDebut) { this.periodeDebut = periodeDebut; }

    public LocalDate getPeriodeFin() { return periodeFin; }
    public void setPeriodeFin(LocalDate periodeFin) { this.periodeFin = periodeFin; }

    public double getSalaireBase() { return salaireBase; }
    public void setSalaireBase(double salaireBase) { this.salaireBase = salaireBase; }

    public int getJoursTravailles() { return joursTravailles; }
    public void setJoursTravailles(int joursTravailles) { this.joursTravailles = joursTravailles; }

    public int getJoursAbsence() { return joursAbsence; }
    public void setJoursAbsence(int joursAbsence) { this.joursAbsence = joursAbsence; }

    public int getTotalRetardsMinutes() { return totalRetardsMinutes; }
    public void setTotalRetardsMinutes(int totalRetardsMinutes) { this.totalRetardsMinutes = totalRetardsMinutes; }

    public double getHeuresSupplementaires() { return heuresSupplementaires; }
    public void setHeuresSupplementaires(double heuresSupplementaires) { this.heuresSupplementaires = heuresSupplementaires; }

    public double getDeductionAbsences() { return deductionAbsences; }
    public void setDeductionAbsences(double deductionAbsences) { this.deductionAbsences = deductionAbsences; }

    public double getDeductionRetards() { return deductionRetards; }
    public void setDeductionRetards(double deductionRetards) { this.deductionRetards = deductionRetards; }

    public double getSalaireNet() { return salaireNet; }
    public void setSalaireNet(double salaireNet) { this.salaireNet = salaireNet; }

    public boolean isEnvoye() { return envoye; }
    public void setEnvoye(boolean envoye) { this.envoye = envoye; }
}
