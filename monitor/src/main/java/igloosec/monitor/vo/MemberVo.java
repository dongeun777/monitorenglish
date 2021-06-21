package igloosec.monitor.vo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class MemberVo implements UserDetails {
    private String email;
    private String passwd;
    private String auth;
    private boolean enabled;
    private String isAccountNonexpired;
    private String isAccountNonLocked;
    private String isCredentialsNonExpired;
    private String regDate;
    private String ipAddr;
    private String rscGrp;
    private String enableVal;
    private String company;
    private String step;
    private String deleteVal;
    private String id;

    //shell parameter
    private String totalParam;

    private String qrcord;
    private String secretkey;

    private String mfacode;
    private String logid;

    private String costInBillingCurrency;

    private String usage_data;

    private String cloud_vendor;

    private String name;

    private String vmseries;
    private String disksize;




    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }


    public String getIsAccountNonexpired() {
        return isAccountNonexpired;
    }

    public void setIsAccountNonexpired(String isAccountNonexpired) {
        this.isAccountNonexpired = isAccountNonexpired;
    }

    public String getIsAccountNonLocked() {
        return isAccountNonLocked;
    }

    public void setIsAccountNonLocked(String isAccountNonLocked) {
        this.isAccountNonLocked = isAccountNonLocked;
    }

    public String getIsCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    public void setIsCredentialsNonExpired(String isCredentialsNonExpired) {
        this.isCredentialsNonExpired = isCredentialsNonExpired;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> authority = new ArrayList<GrantedAuthority>();
        authority.add(new SimpleGrantedAuthority(auth));
        return authority;



    }

    @Override
    public String getPassword() {
        return passwd;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getRscGrp() {
        return rscGrp;
    }

    public void setRscGrp(String rscGrp) {
        this.rscGrp = rscGrp;
    }

    public String getEnableVal() {
        return enableVal;
    }

    public void setEnableVal(String enableVal) {
        this.enableVal = enableVal;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getTotalParam() {
        return totalParam;
    }

    public void setTotalParam(String totalParam) {
        this.totalParam = totalParam;
    }

    public String getDeleteVal() {
        return deleteVal;
    }

    public void setDeleteVal(String deleteVal) {
        this.deleteVal = deleteVal;
    }

    public String getQrcord() {
        return qrcord;
    }

    public void setQrcord(String qrcord) {
        this.qrcord = qrcord;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }


    public String getMfacode() {
        return mfacode;
    }

    public void setMfacode(String mfacode) {
        this.mfacode = mfacode;
    }

    public String getLogid() {
        return logid;
    }

    public void setLogid(String logid) {
        this.logid = logid;
    }

    public String getCostInBillingCurrency() {
        return costInBillingCurrency;
    }

    public void setCostInBillingCurrency(String costInBillingCurrency) {
        this.costInBillingCurrency = costInBillingCurrency;
    }

    public String getUsage_data() {
        return usage_data;
    }

    public void setUsage_data(String usage_data) {
        this.usage_data = usage_data;
    }

    public String getCloud_vendor() {
        return cloud_vendor;
    }

    public void setCloud_vendor(String cloud_vendor) {
        this.cloud_vendor = cloud_vendor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVmseries() {
        return vmseries;
    }

    public void setVmseries(String vmseries) {
        this.vmseries = vmseries;
    }

    public String getDisksize() {
        return disksize;
    }

    public void setDisksize(String disksize) {
        this.disksize = disksize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
