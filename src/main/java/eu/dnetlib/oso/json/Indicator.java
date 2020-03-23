package eu.dnetlib.oso.json;

public class Indicator {
    Integer oa;
    Float percentage;
    Integer total;

    public Indicator(Integer oa, Float percentage, Integer total) {
        this.oa = oa;
        this.percentage = percentage;
        this.total = total;
    }

    public Indicator() {
    }

    public Integer getOa() {
        return oa;
    }

    public void setOa(Integer oa) {
        this.oa = oa;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
