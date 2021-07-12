package igloosec.monitor.vo;

public class LeadsInfoVo {

    private String partitionKey;
    private String rowKey;
    private String timestamp;
    private String productId;
    private String leadSource;
    private String actionCode;
    private String publisherDisplayName;
    private String offerDisplayName;
    private String createdTime;
    private String description;
    private String customerCountry;
    private String vendor;
    private String emailAddr;
    private String rscGrp;


    /* customerInfo */
    private String FirstName;
    private String LastName;
    private String Email;
    private String Phone;
    private String Country;
    private String Company;
    private String Title;

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getLeadSource() {
        return leadSource;
    }

    public void setLeadSource(String leadSource) {
        this.leadSource = leadSource;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public void setPublisherDisplayName(String publisherDisplayName) {
        this.publisherDisplayName = publisherDisplayName;
    }

    public String getOfferDisplayName() {
        return offerDisplayName;
    }

    public void setOfferDisplayName(String offerDisplayName) {
        this.offerDisplayName = offerDisplayName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    @Override
    public String toString() {
        return "LeadsInfoVo{" +
                "partitionKey='" + partitionKey + '\'' +
                ", rowKey='" + rowKey + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", productId='" + productId + '\'' +
                ", leadSource='" + leadSource + '\'' +
                ", actionCode='" + actionCode + '\'' +
                ", publisherDisplayName='" + publisherDisplayName + '\'' +
                ", offerDisplayName='" + offerDisplayName + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", description='" + description + '\'' +
                ", FirstName='" + FirstName + '\'' +
                ", LastName='" + LastName + '\'' +
                ", Email='" + Email + '\'' +
                ", Phone='" + Phone + '\'' +
                ", Country='" + Country + '\'' +
                ", Company='" + Company + '\'' +
                ", Title='" + Title + '\'' +
                '}';
    }

    public String getCustomerCountry() {
        return customerCountry;
    }

    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getRscGrp() {
        return rscGrp;
    }

    public void setRscGrp(String rscGrp) {
        this.rscGrp = rscGrp;
    }
}