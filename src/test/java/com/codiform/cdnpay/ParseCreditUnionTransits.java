package com.codiform.cdnpay;

import java.io.IOException;
import java.util.List;

public class ParseCreditUnionTransits {

	public static void main(String[] arguments) throws IOException {
		Parser parser = new Parser( "src/test/resources/MBRCRUNN.pdf" );
		List<TransitRoutingNumber> transits = parser.getTransitRoutingNumbers();
		System.out.printf( "%d transit routing numbers found", transits.size() );
	}

}
