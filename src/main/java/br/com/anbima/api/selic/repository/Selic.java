package br.com.anbima.api.selic.repository;

/**
 * Class Data Selic
 * 
 * @author oscaroaj
 *
 */
public class Selic {

	public String date;
	public float taxa;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public float getTaxa() {
		return taxa;
	}

	public void setTaxa(float taxa) {
		this.taxa = taxa;
	}

}
