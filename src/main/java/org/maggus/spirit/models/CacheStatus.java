package org.maggus.spirit.models;

import lombok.AllArgsConstructor;
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
public class CacheStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @CacheIndex
    private String externalUrl;
    private Long lastUpdatedMs;
    private Long spentMs;

    public CacheStatus(String url, Long now, Long spent){
        this.externalUrl = url;
        this.lastUpdatedMs = now;
        this.spentMs = spent;
    }
}
