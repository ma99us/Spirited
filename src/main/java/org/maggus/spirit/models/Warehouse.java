package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
public class Warehouse extends CacheItem{

    private String address;
    private String city;

    public Warehouse(String store, String address, String city){
        super();
        setName(store);
        this.address = address;
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warehouse warehouse = (Warehouse) o;
        return Objects.equals(getName(), warehouse.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public String toSimpleString() {
        return "Warehouse{" + getName() +
                " - " + getAddress() +
                " - " + city +
                '}';
    }

    /**
     * Unfortunately since we use Warehouse as aObject key in a Quantities map in Whisky,
     * which we need to serialize into json for the rest api response.
     * We have to make sure that toString() returns valid json object.
     */
    @Override
    public String toString() {
        final JsonObjectBuilder builder = Json.createBuilderFactory(null).createObjectBuilder();
        return builder.add("store", getName())
                .add("address", getAddress())
                .add("city", getCity())
                .build().toString();
    }

}
