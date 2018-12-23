package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
public class FlavorProfile extends CacheItem {

    @Id
    @GeneratedValue(generator = "FlavorProfileGen", strategy = GenerationType.AUTO)
    private long id;
    @CacheIndex
    private String name;            // Whisky name
    private String flavors;         // flavor summary tags
    private Integer smoky;
    private Integer peaty;
    private Integer spicy;
    private Integer herbal;
    private Integer oily;
    private Integer full_bodied;
    private Integer rich;
    private Integer sweet;
    private Integer briny;
    private Integer salty;
    private Integer vanilla;
    private Integer tart;
    private Integer fruity;
    private Integer floral;

    public FlavorProfile(String name, String url) {
        super();
        setName(name);
        setCacheExternalUrl(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlavorProfile wc = (FlavorProfile) o;
        return Objects.equals(getName(), wc.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

}
