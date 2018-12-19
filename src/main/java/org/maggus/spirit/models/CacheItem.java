package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class CacheItem {
    private String cacheExternalUrl;        // ANBL site data page url
    private Long cacheLastUpdatedMs;        // timestamp of last update from ANBL
    private Long cacheSpentMs;              // time spent building this cache
}
