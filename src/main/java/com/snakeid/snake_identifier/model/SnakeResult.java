package com.snakeid.snake_identifier.model;
import lombok.Data;
import java.util.List;

@Data
public class SnakeResult {
    private String commonName;
    private String hindiName;
    private String scientificName;
    private Boolean venomous;
    private String venomType;
    private String dangerLevel;
    private List<String> foundInRegions;
    private List<String> firstAid;
    private List<String> symptoms;
    private Double confidence;
    private String notes;
    private Boolean isSnake;
}