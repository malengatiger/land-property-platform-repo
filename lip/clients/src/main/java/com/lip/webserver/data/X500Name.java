package com.lip.webserver.data;

public class X500Name {
    private String organisation, locality, country;

    public X500Name(String organisation, String locality, String country) {
        this.organisation = organisation;
        this.locality = locality;
        this.country = country;
    }

    public X500Name() {
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
