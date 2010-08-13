package com.codiform.cdnpay;

public class TransitRoutingNumber {

	private int financialInstitutionNumber;

	private String financialInstitutionName;

	private String electronicTransit;

	private String paperTransit;

	private String address;

	public TransitRoutingNumber(
			int financialInstitutionNumber, String financialInstitutionName,
			String electronicTransit, String paperTransit,
			String address) {
		this.financialInstitutionNumber = financialInstitutionNumber;
		this.financialInstitutionName = financialInstitutionName;
		this.electronicTransit = electronicTransit;
		this.paperTransit = paperTransit;
		this.address = address;
	}

	public int getFinancialInstitutionNumber() {
		return financialInstitutionNumber;
	}

	public String getFinancialInstitutionName() {
		return financialInstitutionName;
	}

	public String getElectronicTransit() {
		return electronicTransit;
	}

	public String getPaperTransit() {
		return paperTransit;
	}

	public String getAddress() {
		return address;
	}

	public void appendAddress(String addressCompletion) {
		address += addressCompletion;
	}

}
