package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
public class WhiskyCategory extends CacheItem {

    @Id
    @GeneratedValue(generator="WhiskyCategoryGen", strategy = GenerationType.AUTO)
    private long id;
    @CacheIndex
    private String name;
    private String country;         // country of origin
    private String region;          // region within Country if available
    private String type;            // should be one of the items in WHISKY_TYPES enum

    public WhiskyCategory(String name, String url) {
        super();
        setName(name);
        setCacheExternalUrl(url);
    }

    public WhiskyCategory(String name, String url, Long now, Long spent) {
        this(name, url);
        setCacheLastUpdatedMs(now);
        setCacheSpentMs(spent);
    }

    public WhiskyCategory(String name, String url, String country, String region, String type) {
        this(name, url);
        setCountry(country);
        setRegion(region);
        setType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhiskyCategory wc = (WhiskyCategory) o;
        return Objects.equals(getName(), wc.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

//    @Override
//    public String toString() {
//        final JsonObjectBuilder builder = Json.createBuilderFactory(null).createObjectBuilder();
//        return builder.add("category", getName())
//                .add("country", getCountry())
//                .add("region", getRegion())
//                .add("type", getType())
//                .build().toString();
//    }
}
