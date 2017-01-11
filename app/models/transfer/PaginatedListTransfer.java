package models.transfer;

import com.fasterxml.jackson.annotation.JsonView;
import models.Contribution;
import models.misc.Views;

import java.util.List;

public class PaginatedListTransfer<T> {

    @JsonView(Views.Public.class)
    private int pageSize;

    @JsonView(Views.Public.class)
    private int page;

    @JsonView(Views.Public.class)
    private int total;

    @JsonView(Views.Public.class)
    private List<T> list;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
