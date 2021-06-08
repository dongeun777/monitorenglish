package igloosec.monitor.vo;

import com.microsoft.azure.storage.table.TableServiceEntity;

import java.util.Date;

public class CustomerEntity extends TableServiceEntity {
    public CustomerEntity(String lastName, String firstName) {
        this.partitionKey = lastName;
        this.rowKey = firstName;
    }

    public CustomerEntity() { }

    private String productId;
    private String customerInfo;
    private String leadSource;
    private String actionCode;
    private String publisherDisplayName;
    private String offerDisplayName;
    private String createdTime;
    private String description;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo;
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

    public Date getTimeStamp() {
        return getTimestamp();
    }

    public String getPartitionKey() {
        return this.partitionKey;
    }

    public String getRowKey() {
        return this.rowKey;
    }

    @Override
    public String toString() {
        return "CustomerEntity{" +
                "partitionKey='" + getPartitionKey() + '\'' +
                ", rowKey='" + getRowKey() + '\'' +
                ", timestamp='" + getTimeStamp() + '\'' +
                ", productId='" + productId + '\'' +
                ", customerInfo='" + customerInfo + '\'' +
                ", leadSource='" + leadSource + '\'' +
                ", actionCode='" + actionCode + '\'' +
                ", publisherDisplayName='" + publisherDisplayName + '\'' +
                ", offerDisplayName='" + offerDisplayName + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}