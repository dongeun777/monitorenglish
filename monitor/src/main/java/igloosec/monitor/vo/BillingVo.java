package igloosec.monitor.vo;

public class BillingVo {
    private String card;
    private String expiry;
    private String birth;
    private String pg;
    private String billingtype;


    private String email;
    private String paydate;
    private String resource;
    private String log;
    private String billingsum;

    private String cvc;



    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPaydate() {
        return paydate;
    }

    public void setPaydate(String paydate) {
        this.paydate = paydate;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }



    public String getBillingsum() {
        return billingsum;
    }

    public void setBillingsum(String billingsum) {
        this.billingsum = billingsum;
    }

    public String getPg() {
        return pg;
    }

    public void setPg(String pg) {
        this.pg = pg;
    }

    public String getBillingtype() {
        return billingtype;
    }

    public void setBillingtype(String billingtype) {
        this.billingtype = billingtype;
    }

    public String getCvc() { return cvc; }

    public void setCvc(String cvc) { this.cvc = cvc; }

}
