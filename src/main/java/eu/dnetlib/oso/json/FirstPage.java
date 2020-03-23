package eu.dnetlib.oso.json;

public class FirstPage {

    private Overview overview;
    private CountryOverview[] countries;

    public FirstPage() {
    }

    public FirstPage(Overview overview, CountryOverview[] countries) {
        this.overview = overview;
        this.countries = countries;
    }

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }

    public CountryOverview[] getCountries() {
        return countries;
    }

    public void setCountries(CountryOverview[] countries) {
        this.countries = countries;
    }
}
