package org.maggus.spirit.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryMetadata {
    private Integer resultsPerPage;
    private Integer pageNumber;
    private String sortBy;
    private Integer totalResults;
}
