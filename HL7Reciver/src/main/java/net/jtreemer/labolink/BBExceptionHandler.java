package net.jtreemer.labolink;

import java.util.Map;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;

public class BBExceptionHandler implements ReceivingApplicationExceptionHandler {

	public String processException(String arg0, Map<String, Object> arg1, String arg2, Exception arg3)
			throws HL7Exception {
		
		return arg2;
	}

}
