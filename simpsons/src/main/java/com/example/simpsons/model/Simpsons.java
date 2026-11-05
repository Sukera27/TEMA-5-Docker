package com.example.simpsons.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "simpsons")
public class Simpsons implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "simpsons_id")
    Long id;

    @Column(name = "simpsons_name")
    String name;

    @Column(name = "simpsons_pic_url")
    String picUrl;

    @Column(name = "simpsons_date")
    Date fechaConsulta;
}
