package eu.dnetlib.oso.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class StatsRepo {
    @Autowired
    JdbcTemplate jdbcTemplate;

    Overview overview = null;
    CountryOverview[] countries = null;

    public void computeShit() {
        overview = computeOverview();
        countries = computeCountries();
    }

    public FirstPage getFirstPage() {
        if (overview == null || countries == null)
            throw new IllegalStateException("Stats have not been computed");

        FirstPage firstPage = new FirstPage();

        firstPage.setOverview(overview);
        firstPage.setCountries(countries);

        return firstPage;
    }

    public Overview getOverview() {
        return overview;
    }

    public CountryOverview[] getCountries() {
        return countries;
    }

    private Overview computeOverview() {
        final Overview overview = new Overview();

        // get open access numbers
        jdbcTemplate.query("select r.type, count(distinct r.id) from result r join result_datasources rd on rd.id=r.id join datasource d on d.id=rd.datasource join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization left join country c on c.code=o.country where c.continent_name='Europe' and r.type in ('publication', 'dataset', 'software', 'other') and d.type != 'Thematic Repository' and bestlicence='Open Access' group by r.type", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                String type = resultSet.getString(1);
                int absolute = resultSet.getInt(2);

                System.out.println(type);

                switch (type) {
                    case "dataset":
                        overview.setDatasets(new Indicator(absolute, 0f, 0));
                        break;
                    case "publication":
                        overview.setPublications(new Indicator(absolute, 0f, 0));
                        break;
                    case "software":
                        overview.setSoftware(new Indicator(absolute, 0f, 0));
                        break;
                    case "other":
                        overview.setOtherProducts(new Indicator(absolute, 0f, 0));
                        break;
                }
            }
        });

        try {
            System.out.println(new ObjectMapper().writeValueAsString(overview));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // get total numbers
        jdbcTemplate.query("select r.type, count(distinct r.id) from result r join result_datasources rd on rd.id=r.id join datasource d on rd.datasource=d.id join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization left join country c on c.code=o.country where c.continent_name='Europe' and r.type in ('publication', 'dataset', 'other', 'software') and d.type != 'Thematic Repository' group by r.type", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                String type = resultSet.getString(1);
                int total = resultSet.getInt(2);

                switch (type) {
                    case "dataset":
                        overview.getDatasets().setTotal(total);

                        if (total > 0)
                            overview.getDatasets().setPercentage(((float) overview.getDatasets().getOa()) / total * 100f);
                        else
                            overview.getDatasets().setPercentage(null);
                        break;
                    case "publication":
                        overview.getPublications().setTotal(total);

                        if (total > 0)
                            overview.getPublications().setPercentage(((float) overview.getPublications().getOa()) / total * 100f);
                        else
                            overview.getPublications().setPercentage(null);
                        break;
                    case "software":
                        overview.getSoftware().setTotal(total);

                        if (total > 0)
                            overview.getSoftware().setPercentage(((float) overview.getSoftware().getOa()) / total * 100f);
                        else
                            overview.getSoftware().setPercentage(null);
                        break;
                    case "other":
                        overview.getOtherProducts().setTotal(total);

                        if (total > 0)
                            overview.getOtherProducts().setPercentage(((float) overview.getOtherProducts().getOa()) / total * 100f);
                        else
                            overview.getOtherProducts().setPercentage(null);
                        break;
                }
            }
        });

        try {
            System.out.println(new ObjectMapper().writeValueAsString(overview));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        int repositories = jdbcTemplate.queryForObject("select count(distinct d.id) from datasource d join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization join country c on c.code=o.country where c.continent_name='Europe' and d.type in ('Institutional Repository', 'Institutional Repository Aggregator', 'Publication Repository', 'Publication Repository Aggregator')  and exists (select 1 from result r join result_datasources rd on rd.id=r.id and rd.datasource=d.id where r.access_mode='Open Access')", Integer.class);
        overview.setRepositories(new Indicator(repositories, 100f, repositories));

        int journals = jdbcTemplate.queryForObject("select count(distinct d.id) from datasource d join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization join country c on c.code=o.country where c.continent_name='Europe' and d.type in ('Journal', 'Journal Aggregator/Publisher')  and d.id like 'doaj%' and exists (select 1 from result r join result_datasources rd on r.id=rd.id and rd.datasource=d.id where  r.access_mode='Open Access');", Integer.class);
        overview.setJournals(new Indicator(journals, 100f, journals));

        int funders = jdbcTemplate.queryForObject("select count(distinct funder) from project p join project_organizations po on p.id=po.id join organization o on po.organization=o.id join country c on o.country=c.code where c.continent_name='Europe'", Integer.class);
        overview.setFunders(new Indicator(funders, 100f, funders));

        int ecFundedOrgs = jdbcTemplate.queryForObject("select count(distinct o.id) from project p join project_organizations po on p.id=po.id join organization o on po.organization=o.id join country c on o.country=c.code where c.continent_name='Europe' and p.funder='European Commission'", Integer.class);
        overview.setEcFundedOrganizations(new Indicator(ecFundedOrgs, 100f, ecFundedOrgs));

        try {
            System.out.println(new ObjectMapper().writeValueAsString(overview));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return overview;
    }

    private CountryOverview[] computeCountries() {
        Map<String, CountryOverview> countries = new HashMap<>();

        jdbcTemplate.query("select name from country where continent_name='Europe'", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String country = rs.getString("name");
                CountryOverview overview = new CountryOverview();

                overview.setRepositories(new Indicator(0, null, null));
                overview.setJournals(new Indicator(0, null, null));
                overview.setPublications(new Indicator(0, null, null));
                overview.setDatasets(new Indicator(0, null, null));
                overview.setSoftware(new Indicator(0, null, null));
                overview.setOtherProducts(new Indicator(0, null, null));
                overview.setFunders(new Indicator(0, null, null));
                overview.setEcFundedOrganizations(new Indicator(0,0f,0));

                overview.setCountry(country);

                countries.put(country, overview);
            }
        });

        // oa repositories
        jdbcTemplate.query("select count(distinct d.id), c.name from result r join result_datasources rd on r.id=rd.id join datasource d on d.id=rd.datasource join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization left join country c on c.code=o.country where c.continent_name='Europe' and r.access_mode='Open Access' and d.type in ('Institutional Repository', 'Institutional Repository Aggregator', 'Publication Repository','Publication Repository Aggregator') group by c.name", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                int count = resultSet.getInt(1);
                String country = resultSet.getString(2);
                CountryOverview countryOverview = countries.get(country);

                if (countryOverview == null ) {
                    countryOverview = new CountryOverview();
                    countries.put(country, countryOverview);
                }

                countryOverview.setCountry(country);
                countryOverview.setRepositories(new Indicator(count, null, null));
            }
        });

        //oa journals
        jdbcTemplate.query("select count(distinct d.id), c.name from datasource d join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization join country c on c.code=o.country where c.continent_name='Europe' and d.type in ('Journal', 'Journal Aggregator/Publisher')  and d.id like 'doaj%' and exists (select 1 from result r join result_datasources rd on rd.id=r.id and rd.datasource=d.id where r.access_mode='Open Access') group by c.name", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                int count = resultSet.getInt(1);
                String country = resultSet.getString(2);
                CountryOverview countryOverview = countries.get(country);

                if (countryOverview == null ) {
                    countryOverview = new CountryOverview();
                    countryOverview.setCountry(country);
                    countries.put(country, countryOverview);
                }

                countryOverview.setJournals(new Indicator(count, null, null));
            }
        });

        // oa results
        jdbcTemplate.query("select r.type, c.name, count(distinct r.id) from result r join result_datasources rd on r.id=rd.id join datasource d on rd.datasource=d.id join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization left join country c on c.code=o.country where c.continent_name='Europe' and access_mode='Open Access' and d.type != 'Thematic Repository' group by r.type, c.name", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                String type =resultSet.getString(1);
                String country = resultSet.getString(2);
                int count = resultSet.getInt(3);
                CountryOverview countryOverview = countries.get(country);

                if (countryOverview == null ) {
                    countryOverview = new CountryOverview();
                    countryOverview.setCountry(country);
                    countries.put(country, countryOverview);
                }

                Indicator indicator = new Indicator(count, null, null);
                switch (type) {
                    case "publication":
                        countryOverview.setPublications(indicator);
                        break;
                    case "dataset":
                        countryOverview.setDatasets(indicator);
                        break;
                    case "software":
                        countryOverview.setSoftware(indicator);
                        break;
                    case "other":
                        countryOverview.setOtherProducts(indicator);
                        break;
                }
            }
        });

        //  results
        jdbcTemplate.query("select r.type, c.name, count(distinct r.id) from result r join result_datasources rd on r.id=rd.id join datasource d on rd.datasource=d.id join datasource_organizations dor on dor.id=d.id join organization o on o.id=dor.organization left join country c on c.code=o.country where access_mode='Open Access' and c.continent_name='Europe' and d.type != 'Thematic Repository' group by r.type, c.name", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                String type =resultSet.getString(1);
                String country = resultSet.getString(2);
                int count = resultSet.getInt(3);
                CountryOverview countryOverview = countries.get(country);

                if (countryOverview == null ) {
                    countryOverview = new CountryOverview();
                    countryOverview.setCountry(country);
                    countries.put(country, countryOverview);
                }

                Indicator indicator = new Indicator();

                switch (type) {
                    case "publication":
                        indicator = countryOverview.getPublications();

                        if (indicator == null) {
                            indicator = new Indicator(0,0f, count);
                            countryOverview.setPublications(indicator);
                        }
                        break;
                    case "dataset":
                        indicator = countryOverview.getDatasets();

                        if (indicator == null) {
                            indicator = new Indicator(0,0f, count);
                            countryOverview.setDatasets(indicator);
                        }
                        break;
                    case "software":
                        indicator = countryOverview.getSoftware();

                        if (indicator == null) {
                            indicator = new Indicator(0,0f, count);
                            countryOverview.setSoftware(indicator);
                        }
                        break;
                    case "other":
                        indicator = countryOverview.getOtherProducts();

                        if (indicator == null) {
                            indicator = new Indicator(0,0f, count);
                            countryOverview.setOtherProducts(indicator);
                        }
                        break;
                }

                indicator.setTotal(count);

                if (count > 0)
                    indicator.setPercentage(((float)indicator.getOa())/count*100);
                else
                    indicator.setPercentage(null);
            }
        });

        // funders
        jdbcTemplate.query("select c.name, count(distinct funder) from project p join project_organizations po on p.id=po.id join organization o on po.organization=o.id join country c on o.country=c.code where c.continent_name='Europe' group by c.name", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                String country = resultSet.getString(1);
                int count = resultSet.getInt(2);
                CountryOverview countryOverview = countries.get(country);

                if (countryOverview == null ) {
                    countryOverview = new CountryOverview();
                    countryOverview.setCountry(country);
                    countries.put(country, countryOverview);
                }

                countryOverview.setFunders(new Indicator(count, null, null));
            }
        });

        // total funders
        jdbcTemplate.query("select count(f.doi), c.name from fundref f join country c on c.code=f.country where c.continent_name='Europe' group by c.name", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                int count = resultSet.getInt(1);
                String country = resultSet.getString(2);
                CountryOverview countryOverview = countries.get(country);

                if (countryOverview == null ) {
                    countryOverview = new CountryOverview();
                    countryOverview.setCountry(country);
                    countries.put(country, countryOverview);
                }

                Indicator funders = countryOverview.getFunders();

                if (funders == null) {
                    funders = new Indicator(0, 0f, count);
                    countryOverview.setFunders(funders);
                }

                funders.setTotal(count);

                if (count > 0)
                    funders.setPercentage(((float) funders.getOa())/count*100);
            }
        });

        jdbcTemplate.query("select c.name, count(distinct o.id) from project p join project_organizations po on p.id=po.id join organization o on po.organization=o.id join country c on o.country=c.code where c.continent_name='Europe' and p.funder='European Commission' group by c.name", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                String country = resultSet.getString(1);
                int count = resultSet.getInt(2);
                CountryOverview countryOverview = countries.get(country);

                if (countryOverview == null ) {
                    countryOverview = new CountryOverview();
                    countryOverview.setCountry(country);
                    countries.put(country, countryOverview);
                }

                countryOverview.setEcFundedOrganizations(new Indicator(count, null, null));
            }
        });


        return countries.values().toArray(new CountryOverview[0]);
    }
}