package igloosec.monitor.vo;

public class ResourceVo {

    private String id;
    private String date;
    private String MeterCategory;
    private String SubscriptionName;
    private String quantity;
    private String unitOfMeasure;
    private String costInBillingCurrency;
    private String meterSubCategory;
    private String rscparam;

    // disk usage
    private int cpu;
    private int memory;
    private String diskName;
    private int priority;
    private int limits;
    private String total;
    private String free;
    private String current;
    private String data;
    private String usageDiskPer;
    private String usages;
    private String updateTm;

    private int diskSize;
    private double diskPrice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMeterCategory() {
        return MeterCategory;
    }

    public void setMeterCategory(String meterCategory) {
        MeterCategory = meterCategory;
    }

    public String getSubscriptionName() {
        return SubscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        SubscriptionName = subscriptionName;
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

    public String getMeterSubCategory() {
        return meterSubCategory;
    }

    public void setMeterSubCategory(String meterSubCategory) {
        this.meterSubCategory = meterSubCategory;
    }

    public String getRscparam() {
        return rscparam;
    }

    public void setRscparam(String rscparam) {
        this.rscparam = rscparam;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public String getDiskName() {
        return diskName;
    }

    public void setDiskName(String diskName) {
        this.diskName = diskName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getLimits() {
        return limits;
    }

    public void setLimits(int limits) {
        this.limits = limits;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getFree() {
        return free;
    }

    public void setFree(String free) {
        this.free = free;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUsageDiskPer() {
        return usageDiskPer;
    }

    public void setUsageDiskPer(String usageDiskPer) {
        this.usageDiskPer = usageDiskPer;
    }

    public String getUsages() {
        return usages;
    }

    public void setUsages(String usages) {
        this.usages = usages;
    }

    public String getUpdateTm() {
        return updateTm;
    }

    public void setUpdateTm(String updateTm) {
        this.updateTm = updateTm;
    }

    public int getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(int diskSize) {
        this.diskSize = diskSize;
    }

    public double getDiskPrice() {
        return diskPrice;
    }

    public void setDiskPrice(double diskPrice) {
        this.diskPrice = diskPrice;
    }
}
