package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.annotations.CacheIndex;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class WhiskyCategory extends CacheItem{

//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long id;
//    @CacheIndex
//    private String cacheExternalUrl;
//    private Long cacheLastUpdatedMs;
//    private Long cacheSpentMs;

    public WhiskyCategory(String name, String url, Long now, Long spent){
        setName(name);
        setCacheExternalUrl(url);
        setCacheLastUpdatedMs(now);
        setCacheSpentMs(spent);
    }
}
