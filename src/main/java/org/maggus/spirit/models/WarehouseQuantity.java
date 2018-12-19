package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
public class WarehouseQuantity {

//    @Id
//    @GeneratedValue(generator="WarehouseQuantityGen", strategy = GenerationType.AUTO)
//    @ManyToOne(fetch=FetchType.LAZY)
//    @JoinColumn(name="whisky_id")
//    private Whisky whisky;
    private String name;
    private String address;
    private String city;
    private Integer quantity;

    public WarehouseQuantity(String name, String address, String city, Integer quantity) {
        setName(name);
        setAddress(address);
        setCity(city);
        setQuantity(quantity);
    }

    public Warehouse buildWarehouse(){
        return new Warehouse(getName(), getAddress(), getCity());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarehouseQuantity that = (WarehouseQuantity) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
