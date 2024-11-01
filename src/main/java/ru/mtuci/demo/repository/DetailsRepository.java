package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.demo.model.Details;

@Repository
public interface DetailsRepository extends JpaRepository<Details, Long> {
}
