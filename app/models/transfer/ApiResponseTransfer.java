package models.transfer;

import java.util.List;

/**
 * This class helps standarize API responses when
 * the backend is asked for a list of elements.
 *
 * @author Jorge Ramírez <jorge@codium.com.py>
 **/
public class ApiResponseTransfer<M> {

    Integer pageSize;
    Integer totalCount;
    List<M> results;


    public List<M> getResults() {
        return results;
    }

    public void setResults(List<M> results) {
        this.results = results;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
