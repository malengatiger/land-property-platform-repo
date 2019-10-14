package com.lip.webserver.data;

public class LIPAccountDTO {
    private X500Name accountInfoName, lipAccountName;
    private String identifier, name;
    private String  email;
    private String cellphone;

    public LIPAccountDTO() {
    }

    public LIPAccountDTO(X500Name accountInfoName, X500Name lipAccountName,
                         String identifier, String name, String email, String cellphone) {
        this.accountInfoName = accountInfoName;
        this.lipAccountName = lipAccountName;
        this.identifier = identifier;
        this.name = name;
        this.email = email;
        this.cellphone = cellphone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public X500Name getAccountInfoName() {
        return accountInfoName;
    }

    public void setAccountInfoName(X500Name accountInfoName) {
        this.accountInfoName = accountInfoName;
    }

    public X500Name getLipAccountName() {
        return lipAccountName;
    }

    public void setLipAccountName(X500Name lipAccountName) {
        this.lipAccountName = lipAccountName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
