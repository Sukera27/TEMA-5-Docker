package com.example.simpsons.controllers;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.simpsons.model.Simpsons;
import com.example.simpsons.service.SimpsonsService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/api/v1/simpson")
@CrossOrigin
@AllArgsConstructor
public class SimpsonsController {

    SimpsonsService simpsonsService;

    @GetMapping
    public List<Simpsons> requestAllSimpsons() {

        return simpsonsService.searchAll();
    }
    @PostMapping
    void insertSimpsons(@RequestBody Simpsons simpsons) {
        simpsonsService.addSimpsons(simpsons);
    }
}
