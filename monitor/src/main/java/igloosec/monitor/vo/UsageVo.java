package igloosec.monitor.vo;

public class UsageVo {

    private String id;
    private String usage_data;
    private String usage_money;
    private String idate;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
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


    public String getUsage_money() {
        return usage_money;
    }

    public void setUsage_money(String usage_money) {
        this.usage_money = usage_money;
    }
}
