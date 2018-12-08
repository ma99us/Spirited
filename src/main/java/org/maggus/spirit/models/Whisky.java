package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Whisky {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private Boolean inactive;
    @CacheIndex
    private String name;
    private String country;
    private String region;
    private String unitVolume;      //TODO: convert to number?
    private Integer unitPrice;

    public Whisky(String name, String volume, Integer price) {
        this.name = name;
        this.unitVolume = volume;
        this.unitPrice = price;
    }
}
