package eu.dnetlib.oso.json;

public class Overview {

    Indicator publications = new Indicator(0,0f,0);
    Indicator datasets = new Indicator(0,0f,0);
    Indicator repositories;
    Indicator journals;
    Indicator policies = new Indicator(null, null, null);
    Indicator software = new Indicator(0,0f,0);
    Indicator otherProducts = new Indicator(0,0f,0);
    Indicator funders;
    Indicator ecFundedOrganizations;


    public Overview() {
    }

    public Indicator getPublications() {
        return publications;
    }

    public void setPublications(Indicator publications) {
        this.publications = publications;
    }

    public Indicator getDatasets() {
        return datasets;
    }

    public void setDatasets(Indicator datasets) {
        this.datasets = datasets;
    }

    public Indicator getRepositories() {
        return repositories;
    }

    public void setRepositories(Indicator repositories) {
        this.repositories = repositories;
    }

    public Indicator getJournals() {
        return journals;
    }

    public void setJournals(Indicator journals) {
        this.journals = journals;
    }

    public Indicator getPolicies() {
        return policies;
    }

    public void setPolicies(Indicator policies) {
        this.policies = policies;
    }

    public Indicator getSoftware() {
        return software;
    }

    public void setSoftware(Indicator software) {
        this.software = software;
    }

    public Indicator getOtherProducts() {
        return otherProducts;
    }

    public void setOtherProducts(Indicator otherProducts) {
        this.otherProducts = otherProducts;
    }

    public Indicator getFunders() {
        return funders;
    }

    public void setFunders(Indicator funders) {
        this.funders = funders;
    }

    public Indicator getEcFundedOrganizations() {
        return ecFundedOrganizations;
    }

    public void setEcFundedOrganizations(Indicator ecFundedOrganizations) {
        this.ecFundedOrganizations = ecFundedOrganizations;
    }
}