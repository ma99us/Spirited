package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
public class WhiskyCategory extends CacheItem{

    public WhiskyCategory(String name, String url, Long now, Long spent){
        super();
        setName(name);
        setCacheExternalUrl(url);
        setCacheLastUpdatedMs(now);
        setCacheSpentMs(spent);
    }
}
