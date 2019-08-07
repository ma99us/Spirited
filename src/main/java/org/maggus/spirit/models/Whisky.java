package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
public class Whisky extends CacheItem implements Cloneable {

    @Id
    @GeneratedValue(generator="WhiskyGen", strategy = GenerationType.AUTO)
    private long id;
    private Boolean inactive;       // if 'true' then it should be excluded from all queries
    //// basic info
    @CacheIndex
    private String name;
    private String country;         // country of origin
    private String region;          // region within Country if available
    private Integer unitVolumeMl;   // bottle volume in milliliters
    private Integer qtyPerContainer;    // number of bottles/cans in one pack
    private BigDecimal unitPrice;   // decimal price in dollars CAD
    private String thumbnailUrl;    // url or path to small imabge of the bottle
    private String type;            // should be one of the items in WHISKY_TYPES enum
    //// detailed info
    private String productCode;    // ANBL Product Code
    private Double alcoholContent;  // Alcohol content, like 40.0%
    @Column(columnDefinition = "text")
    private String description;     // poetic description of the whisky flavour
    //@OneToMany(mappedBy = "whisky", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ElementCollection
    private Set<WarehouseQuantity> quantities = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private FlavorProfile flavorProfile;

    public Whisky(String name, Integer volumeMl, BigDecimal price) {
        super();
        setName(name);
        setUnitVolumeMl(volumeMl);
        setUnitPrice(price);
    }

    public void setStoreQuantity(WarehouseQuantity wq){
        quantities.add(wq);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Whisky whisky = (Whisky) o;
        return Objects.equals(getProductCode(), whisky.getProductCode()) ||
                (Objects.equals(getName(), whisky.getName()) &&
                Objects.equals(getCountry(), whisky.getCountry()) &&
                Objects.equals(getUnitVolumeMl(), whisky.getUnitVolumeMl()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductCode(), getName(), getCountry(), getUnitVolumeMl());
    }

    @Override
    public String toString() {
        return "Whisky{id=" + getId() +
                ", " + getName() +
                " " + getUnitVolumeMl() + "ml"+
                " - " + getCountry() +
                '}';
    }

    public void mergeFrom(Whisky w){
        if(w == null){
            return;
        }
        if(w.getCacheExternalUrl() != null) {setCacheExternalUrl(w.getCacheExternalUrl());}
        if(w.getCacheLastUpdatedMs() != null) {setCacheLastUpdatedMs(w.getCacheLastUpdatedMs());}
        if(w.getCacheSpentMs() != null) {setCacheSpentMs(w.getCacheSpentMs());}
        //
        if(w.getId() > 0) {setId(w.getId());};
        if(w.getName() != null) {setName(w.getName());};
        if(w.getCountry() != null) {setCountry(w.getCountry());};
        if(w.getRegion() != null) {setRegion(w.getRegion());};
        if(w.getUnitVolumeMl() != null) {setUnitVolumeMl(w.getUnitVolumeMl());};
        if(w.getUnitPrice() != null) {setUnitPrice(w.getUnitPrice());};
        if(w.getThumbnailUrl() != null) {setThumbnailUrl(w.getThumbnailUrl());};
        if(w.getType() != null) {setType(w.getType());};
        if(w.getProductCode() != null) {setProductCode(w.getProductCode());};
        if(w.getQtyPerContainer() != null) {setQtyPerContainer(w.getQtyPerContainer());};
        if(w.getAlcoholContent() != null) {setAlcoholContent(w.getAlcoholContent());};
        if(w.getDescription() != null) {setDescription(w.getDescription());};
        //
        if(w.getQuantities() != null) {setQuantities(w.getQuantities());};
        if(w.getFlavorProfile() != null) {setFlavorProfile(w.getFlavorProfile());};
    }

    @Override
    public Whisky clone() {
        Whisky clone = new Whisky(getName(), getUnitVolumeMl(), getUnitPrice());
        clone.mergeFrom(this);
        return clone;
    }
}
