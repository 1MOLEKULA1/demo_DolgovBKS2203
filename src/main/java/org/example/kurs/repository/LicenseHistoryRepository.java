package org.example.kurs.repository;

import org.example.kurs.model.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
    // Дополнительные методы, если нужно
}
