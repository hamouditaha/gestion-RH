package com.gestionpresence.repository;

import com.gestionpresence.model.BulletinSalaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BulletinSalaireRepository extends JpaRepository<BulletinSalaire, Long> {

    List<BulletinSalaire> findByEmployeeId(Long employeeId);

    Optional<BulletinSalaire> findByEmployeeIdAndPeriodeDebutAndPeriodeFin(Long employeeId,
                                                                          LocalDate periodeDebut,
                                                                          LocalDate periodeFin);

    @Query("SELECT b FROM BulletinSalaire b WHERE b.employee.id = :employeeId " +
           "AND b.periodeDebut >= :debut AND b.periodeFin <= :fin")
    List<BulletinSalaire> findByEmployeeIdAndPeriode(@Param("employeeId") Long employeeId,
                                                    @Param("debut") LocalDate debut,
                                                    @Param("fin") LocalDate fin);

    List<BulletinSalaire> findByEnvoyeFalse();
}
