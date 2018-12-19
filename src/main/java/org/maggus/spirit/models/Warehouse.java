package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(generator="WarehouseGen", strategy = GenerationType.AUTO)
    private long id;
    @CacheIndex
    private String name;
    private String address;
    private String city;
//    @OneToMany(mappedBy = "warehouse", fetch=FetchType.LAZY)
//    private List<WarehouseQuantity> quantities = new ArrayList<>();

    public Warehouse(String store, String address, String city){
        setName(store);
        setAddress(address);
        setCity(city);
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

    public String toString() {
        return "Warehouse{id=" + getId() +
                "; " + getName() +
                "; " + getAddress() +
                "; " + city +
                '}';
    }

    /**
     * Unfortunately since we use Warehouse as aObject key in a Quantities map in Whisky,
     * which we need to serialize into json for the rest api response.
     * We have to make sure that toString() returns valid json object.
     */
//    @Override
//    public String toString() {
//        final JsonObjectBuilder builder = Json.createBuilderFactory(null).createObjectBuilder();
//        return builder.add("store", getName())
//                .add("address", getAddress())
//                .add("city", getCity())
//                .build().toString();
//    }

}
