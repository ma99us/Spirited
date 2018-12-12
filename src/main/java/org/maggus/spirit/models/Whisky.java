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
public class Whisky extends CacheItem {

//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long id;
    private Boolean inactive;       // if 'true' then it should be excluded from all queries
//    @CacheIndex
//    private String name;
    private String country;
    private String region;          // region within Country if available
    private Integer unitVolumeMl;   // bottle volume in milliliters
    private BigDecimal unitPrice;   // decimal price in dollars CAD
    private String thumbnailUrl;    // url or path to small imabge of the bottle
    private String description;     // poetic description of the whisky flavour
    private String type;            // should be one of the items in WHISKY_TYPES enum

    private String anblProdCode;    // ANBL Product Code
    private Double alcoholContent;  // Alcohol content, like 40.0%

    public Whisky(String name, Integer volumeMl, BigDecimal price) {
        super();
        setName(name);
        setUnitVolumeMl(volumeMl);
        setUnitPrice(price);
    }
}
