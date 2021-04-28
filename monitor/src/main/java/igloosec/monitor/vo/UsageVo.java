package igloosec.monitor.vo;

public class UsageVo {

    private String id;
    private String usage_data;
    private String usage_money;
    private String idate;
    private String meterCategory;
    private String meterSubCategory;
    private String quantity;
    private String unitOfMeasure;
    private String costInBillingCurrency;
    private String paramDate;
    private String paraYear;
    private String paraMonth;


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

    public String getMeterCategory() {
        return meterCategory;
    }

    public void setMeterCategory(String meterCategory) {
        this.meterCategory = meterCategory;
    }

    public String getMeterSubCategory() {
        return meterSubCategory;
    }

    public void setMeterSubCategory(String meterSubCategory) {
        this.meterSubCategory = meterSubCategory;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getCostInBillingCurrency() {
        return costInBillingCurrency;
    }

    public void setCostInBillingCurrency(String costInBillingCurrency) {
        this.costInBillingCurrency = costInBillingCurrency;
    }

    public String getParamDate() {
        return paramDate;
    }

    public void setParamDate(String paramDate) {
        this.paramDate = paramDate;
    }


    public String getParaYear() {
        return paraYear;
    }

    public void setParaYear(String paraYear) {
        this.paraYear = paraYear;
    }

    public String getParaMonth() {
        return paraMonth;
    }

    public void setParaMonth(String paraMonth) {
        this.paraMonth = paraMonth;
    }
}
