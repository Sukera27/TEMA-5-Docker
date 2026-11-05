package com.example.simpsons.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimpsonsRepository extends JpaRepository<Simpsons, Long> {

}
