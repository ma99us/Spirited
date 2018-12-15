package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
public class Whisky extends CacheItem {

    // basic info
    private Boolean inactive;       // if 'true' then it should be excluded from all queries
    private String country;
    private String region;          // region within Country if available
    private Integer unitVolumeMl;   // bottle volume in milliliters
    private BigDecimal unitPrice;   // decimal price in dollars CAD
    private String thumbnailUrl;    // url or path to small imabge of the bottle
    private String type;            // should be one of the items in WHISKY_TYPES enum
    // detailed info
    private String anblProdCode;    // ANBL Product Code
    private Double alcoholContent;  // Alcohol content, like 40.0%
    //@Column(name="DESCRIPTION", length = 512)
    @Column(columnDefinition = "text")
    private String description;     // poetic description of the whisky flavour
    //@ManyToMany(cascade = CascadeType.ALL)
    @ElementCollection
    private Map<Warehouse, Integer> quantities = new HashMap<Warehouse, Integer>();

    public Whisky(String name, Integer volumeMl, BigDecimal price) {
        super();
        setName(name);
        setUnitVolumeMl(volumeMl);
        setUnitPrice(price);
    }

    public void setStoreQuantity(final Warehouse warehouse, int qty){
        //Warehouse wh = quantities.keySet().stream().filter(w -> w.getName().equals(warehouse.getName())).findAny().orElse(warehouse);
        quantities.put(warehouse, qty);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Whisky whisky = (Whisky) o;
        return Objects.equals(getName(), whisky.getName()) &&
                Objects.equals(getCountry(), whisky.getCountry()) &&
                Objects.equals(getUnitVolumeMl(), whisky.getUnitVolumeMl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCountry(), getUnitVolumeMl());
    }

    @Override
    public String toString() {
        return "Whisky{" + getName() +
                " " + getUnitVolumeMl() + "ml"+
                " - " + getCountry() +
                '}';
    }
}
