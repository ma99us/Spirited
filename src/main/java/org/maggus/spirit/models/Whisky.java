package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

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
    private Integer unitVolumeMl;
    private BigDecimal unitPrice;
    private String thumbnailUrl;
    private String description;

    public Whisky(String name, Integer volumeMl, BigDecimal price) {
        this.name = name;
        this.unitVolumeMl = volumeMl;
        this.unitPrice = price;
    }
}
