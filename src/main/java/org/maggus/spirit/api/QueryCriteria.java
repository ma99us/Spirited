package org.maggus.spirit.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryCriteria {
    private String type;
    private String region;
    private String Country;
    //TODO:
}
