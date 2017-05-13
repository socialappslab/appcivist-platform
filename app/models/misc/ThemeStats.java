package models.misc;

public class ThemeStats {
    Integer proposals;
    Integer ideas;
    Integer discussion;
    Integer total;
    Integer totalProposalsIdeas;
    String title;
    String type;

    public Integer getProposals() {
        return proposals;
    }

    public void setProposals(Integer proposals) {
        this.proposals = proposals;
    }

    public Integer getIdeas() {
        return ideas;
    }

    public void setIdeas(Integer ideas) {
        this.ideas = ideas;
    }

    public Integer getDiscussion() {
        return discussion;
    }

    public void setDiscussion(Integer discussion) {
        this.discussion = discussion;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getTotalProposalsIdeas() {
        return totalProposalsIdeas;
    }

    public void setTotalProposalsIdeas(Integer totalProposalsIdeas) {
        this.totalProposalsIdeas = totalProposalsIdeas;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
