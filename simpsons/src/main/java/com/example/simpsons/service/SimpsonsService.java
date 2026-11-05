package com.example.simpsons.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.simpsons.model.Simpsons;
import com.example.simpsons.model.SimpsonsRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SimpsonsService {

    SimpsonsRepository repository;
    public List<Simpsons> searchAll() {
         Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return repository.findAll(sort);
    }

    public void addSimpsons(Simpsons simpsons) {
        repository.save(simpsons);
    }
}
