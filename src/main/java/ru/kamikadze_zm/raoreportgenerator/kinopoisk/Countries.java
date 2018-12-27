package ru.kamikadze_zm.raoreportgenerator.kinopoisk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Countries {

    private static final Logger LOG = LogManager.getLogger(Countries.class);

    private static final String FILE_PATH = "/countries.txt";

    public static final Countries INSTANCE = new Countries();

    private final Map<String, Integer> countries;

    private Countries() {
        this.countries = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(FILE_PATH), StandardCharsets.UTF_8))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.isEmpty()) {
                    continue;
                }
                String[] countriesAndIds = l.split(",");
                String country = countriesAndIds[1].toLowerCase();
                Integer id = Integer.valueOf(countriesAndIds[0]);
                this.countries.put(country, id);
            }
        } catch (Exception e) {
            LOG.warn("Cannot read countries file", e);
        }
    }

    public Integer getCountryId(String countryName) {
        String firstCountry = countryName.split(",")[0].trim().toLowerCase();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Input country: {}, first country: {}", countryName, firstCountry);
        }
        return countries.get(firstCountry);
    }
}
