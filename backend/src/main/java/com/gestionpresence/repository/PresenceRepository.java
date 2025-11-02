package com.gestionpresence.repository;

import com.gestionpresence.model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    List<Presence> findByEmployeeId(Long employeeId);

    List<Presence> findByEmployeeIdAndDateHeurePointageBetween(Long employeeId,
                                                              LocalDateTime debut,
                                                              LocalDateTime fin);

    @Query("SELECT p FROM Presence p WHERE DATE(p.dateHeurePointage) BETWEEN :debut AND :fin")
    List<Presence> findByPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT p FROM Presence p WHERE p.employee.id = :employeeId " +
           "AND DATE(p.dateHeurePointage) BETWEEN :debut AND :fin")
    List<Presence> findByEmployeeIdAndPeriode(@Param("employeeId") Long employeeId,
                                             @Param("debut") LocalDate debut,
                                             @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(p) FROM Presence p WHERE p.employee.id = :employeeId " +
           "AND DATE(p.dateHeurePointage) = :date AND p.typePointage = 'ENTREE'")
    long countEntreesByEmployeeAndDate(@Param("employeeId") Long employeeId,
                                      @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Presence p WHERE p.employee.id = :employeeId " +
           "AND DATE(p.dateHeurePointage) = :date AND p.typePointage = 'SORTIE'")
    long countSortiesByEmployeeAndDate(@Param("employeeId") Long employeeId,
                                      @Param("date") LocalDate date);
}
