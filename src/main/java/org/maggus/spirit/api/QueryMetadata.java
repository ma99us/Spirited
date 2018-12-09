package org.maggus.spirit.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryMetadata {
    private Integer resultsPerPage;
    private Integer pageNumber;
    private String sortBy;
    private Integer totalResults;
}
