package br.com.anbima.api.selic.controller;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.anbima.api.selic.repository.Selic;

/**
 * Class API SELIC
 * 
 * @author oscaroaj
 *
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class EstimateController {

	private static List<Selic> selics = new ArrayList<Selic>();

	EstimateController() throws IOException, ParseException {
		loadJSON();
	}

	/**
	 * Method Get all SELIC estimation data
	 * 
	 * @return
	 */
	@GetMapping("/estimate")
	public List<Selic> GetEstimate() {
		return selics;
	}

	/**
	 * Method Get all SELIC estimation data by year and month
	 * 
	 * @param year
	 * @param month
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@GetMapping("/estimate/month")
	public List<Selic> GetEstimateByMonth(@RequestParam String year,
			@RequestParam(required = false) String month) throws IOException,
			ParseException {

		final String dateStr = month != null ? year.concat("-"
				+ String.format("%02d", Integer.parseInt(month))) : year;
		List<Selic> listFilter = selics.stream()
				.filter(selic -> selic.getDate().contains(dateStr))
				.collect(Collectors.toList());
		return listFilter;
	}

	/**
	 * Method Get all SELIC estimation data by year
	 * 
	 * @param year
	 * @return
	 */
	@GetMapping("/average/year")
	public HashMap<String, Object> GetEstimateAverageByYear(
			@RequestParam String year) {
		HashMap<String, Object> response = new HashMap<>();
		Double average = selics.stream()
				.filter(selic -> selic.getDate().contains(year))
				.mapToDouble(Selic::getTaxa).average().orElse(Double.NaN);

		response.put("year", year);
		response.put("average", average);
		return response;
	}

	/**
	 * Method Get all SELIC accumulated data
	 * 
	 * @return
	 */
	@GetMapping("/average")
	public List<HashMap<String, Object>> GetEstimateAverage() {

		List<HashMap<String, Object>> response = new ArrayList<>();

		HashMap<String, HashMap<String, Object>> values = new HashMap<>();

		selics.stream()
				.forEach(
						obj -> {
							String year = obj.getDate().split("-")[0];

							HashMap<String, Object> value = new HashMap<>();

							if (!values.containsKey(year)) {
								value.put("total", 1);
								value.put("accumulated", obj.getTaxa());
								values.put(year, value);
							} else {
								value = values.get(year);
								value.put("total", Integer.parseInt(value.get(
										"total").toString()) + 1);
								value.put(
										"accumulated",
										Float.parseFloat(value.get(
												"accumulated").toString())
												+ obj.getTaxa());
							}
						});

		values.keySet().forEach(
				key -> {
					HashMap<String, Object> result = new HashMap<>();

					float accumulated = Float.parseFloat(values.get(key)
							.get("accumulated").toString());
					int total = Integer.parseInt(values.get(key).get("total")
							.toString());

					float average = accumulated / total;

					result.put("year", key);
					result.put("average", average);

					response.add(result);
				});

		Collections.sort(response, (o1, o2) -> o1.get("year").toString()
				.compareTo(o2.get("year").toString()));

		return response;
	}

	/**
	 * Method to load data from JSON file
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	private static void loadJSON() throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(new FileReader(
				"ESTIMATIVA_SELIC.JSON"));
		for (int i = 0; i < array.size(); i++) {
			Selic selic = new Selic();
			@SuppressWarnings("unchecked")
			HashMap<String, Object> obj = (HashMap<String, Object>) array
					.get(i);
			selic.setTaxa(Float.parseFloat(obj.get("estimativa_taxa_selic")
					.toString()));
			selic.setDate(obj.get("data_referencia").toString());
			selics.add(selic);
		}

	}

}
