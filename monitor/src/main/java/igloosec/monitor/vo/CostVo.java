package igloosec.monitor.vo;

public class CostVo {
    private String idate;
    private String usage_data;
    private String usage_cost;
    private String rscparam;

    public String getUsage_data() {
        return usage_data;
    }

    public void setUsage_data(String usage_data) {
        this.usage_data = usage_data;
    }


    public String getIdate() {
        return idate;
    }

    public void setIdate(String idate) {
        this.idate = idate;
    }

    public String getUsage_cost() {
        return usage_cost;
    }

    public void setUsage_cost(String usage_cost) {
        this.usage_cost = usage_cost;
    }

    public String getRscparam() {
        return rscparam;
    }

    public void setRscparam(String rscparam) {
        this.rscparam = rscparam;
    }
}
