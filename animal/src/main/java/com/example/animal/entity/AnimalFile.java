package com.example.animal.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimalFile {


    private Long id;
    private Long animalId;
    private String fileName;
    private String filePath;
    private String fileType;


}