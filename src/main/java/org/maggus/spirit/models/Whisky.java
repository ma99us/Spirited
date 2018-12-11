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
    private Boolean inactive;       // if 'true' then it should be excluded from all queries
    @CacheIndex
    private String name;
    private String country;
    private String region;          // region within Country if available
    private Integer unitVolumeMl;   // bottle volume in milliliters
    private BigDecimal unitPrice;   // decimal price in dollars CAD
    private String thumbnailUrl;    // url or path to small imabge of the bottle
    private String description;     // poetic description of the whisky flavour
    private String type;            // should be one of the items in WHISKY_TYPES bellow
    private String anblUrl;         // ANBL site product page url
    private Long cacheLastUpdatedMs;   // timestamp of last update from ANBL

    public Whisky(String name, Integer volumeMl, BigDecimal price) {
        this.name = name;
        this.unitVolumeMl = volumeMl;
        this.unitPrice = price;
    }
}
