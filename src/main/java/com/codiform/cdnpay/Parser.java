package com.codiform.cdnpay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

public class Parser {

	private List<TransitRoutingNumber> transits;
	private Pattern bankPattern = Pattern.compile( "^([A-Z\\(\\),'&\\.\\- ]+)(\\d{3})(\\s\\-\\ Continued)?\\s*$" );
	private Pattern transitPattern = Pattern.compile( "^(\\d{9})\\s+(\\d{5}-\\d{3})\\s+(.*)$" );
	private Pattern transitContinued = Pattern.compile( "^\\s{31}(.*)\\s*$|^(, .*)\\s+" );
	private Pattern noise = Pattern.compile( "_{111}\\s*|\\s+Routing Numbers/\\s+|Electronic\\s+Paper\\(MICR\\)\\s*|Num¯ros d'acheminement\\s+|ƒlectronque.*postale\\s*|\\s+|.+SECTION I NUMERIC LIST / LISTE NUMERIQUE.*" );
	private String currentBankName;
	private Integer currentBankNumber;
	private TransitRoutingNumber currentTransit;

	public Parser(String file) throws IOException {
		PdfReader reader = new PdfReader( file );
		this.transits = new ArrayList<TransitRoutingNumber>();
		read( reader );
	}

	private void read(PdfReader reader) throws IOException {
		PdfTextExtractor extractor = new PdfTextExtractor( reader );
		for( int page = 1; page <= reader.getNumberOfPages(); page++ ) {
			read( extractor.getTextFromPage( page ) );
		}
	}

	private void read(String page) {
		String[] lines = page.split( "\n" );
		for( String line : lines ) {
			if( !noise.matcher( line ).matches() ) {
				parseLine( line );
			}
		}
	}

	private void parseLine(String line) {
		Matcher bankMatcher = bankPattern.matcher( line );
		Matcher transitMatcher = transitPattern.matcher( line );
		Matcher transitContinuedMatcher = transitContinued.matcher( line );
		if( bankMatcher.matches() ) {
			bankParsed( Integer.valueOf( bankMatcher.group( 2 ) ),
					bankMatcher.group( 1 ).trim() );
		} else if( transitMatcher.matches() ) {
			transitParsed( transitMatcher.group( 1 ),
					transitMatcher.group( 2 ), transitMatcher.group( 3 ) );
		} else if( transitContinuedMatcher.matches() ) {
			transitContinuationParsed( line, transitContinuedMatcher.group( 1 ) );
		} else {
			System.err.printf( "No Parsing Instructions for Line:\n%s\n", line );
		}
	}

	private void transitContinuationParsed(String line,
			String transitContinuation) {
		if( currentTransit != null ) {
			currentTransit.appendAddress( transitContinuation );
			currentTransit = null;
		} else {
			System.err.printf( "Cannot find transit to continue with: %s",
					transitContinuation );
		}
	}

	private void transitParsed(String electronicTransit, String paperTransit,
			String address) {
		compareBanks( electronicTransit.substring( 1, 4 ),
				paperTransit.substring( 6, 9 ) );
		currentTransit = new TransitRoutingNumber(
				currentBankNumber, currentBankName,
				electronicTransit, paperTransit, address );
		transits.add( currentTransit );
	}

	private void compareBanks(String electronicTransitBank,
			String paperTransitBank) {
		int eBank = Integer.parseInt( electronicTransitBank );
		int pBank = Integer.parseInt( paperTransitBank );
		if( currentBankNumber.intValue() != eBank ) {
			if( eBank != pBank ) {
				System.err.printf(
						"Section bank number (%d), electronic bank number (%d) and paper (MICR) bank number (%d) do not match.\n",
						currentBankNumber, eBank, pBank );
			} else {
				System.err.printf(
						"Section bank number (%d) and paper (MICR) bank number (%d) match, but the electronic bank number (%d) does not.\n",
						currentBankNumber, pBank, eBank );
			}
		} else if( currentBankNumber.intValue() != pBank ) {
			// this seems common, and not to be a problem
			// System.err.printf(
			// "Section bank number (%d) and electronic bank number (%d) match, but the paper (MICR) bank number (%d) does not.\n",
			// currentBankNumber, eBank, pBank );
		}
	}

	private void bankParsed(Integer newBankNumber, String newBankName) {
		if( currentBankNumber == null
				|| !currentBankNumber.equals( newBankNumber ) ) {
			currentBankName = newBankName;
			currentBankNumber = newBankNumber;
			currentTransit = null;
		}
	}

	public List<TransitRoutingNumber> getTransitRoutingNumbers() {
		return transits;
	}

}
