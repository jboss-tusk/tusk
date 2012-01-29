package org.jboss.tusk.esb.support.util;

import javax.xml.namespace.QName;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jboss.soa.esb.actions.ActionProcessingDetailFaultException;

public class SoapFaultFactory {

    private static final String FAULT = "<ou:fault xmlns:ou='http://www.ohio.edu/schema/fault'><ou:faultCode>%s</ou:faultCode><ou:faultReason>%s</ou:faultReason></ou:fault>";
    private static final String FAULT_THROWABLE = "<ou:fault xmlns:ou='http://www.ohio.edu/schema/fault'><ou:faultCode>%s</ou:faultCode><ou:faultDetail>%s</ou:faultDetail><ou:faultReason>%s</ou:faultReason></ou:fault>";

	private SoapFaultFactory() {
		// seal
	}
	
	public static class Factory {
		public static ActionProcessingDetailFaultException createFault(final String code, final String description)
		{
			return new ActionProcessingDetailFaultException(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), description, String.format(FAULT, code, description)) ;
		}
		
		public static ActionProcessingDetailFaultException createFault(final String code, final String description, final String reason)
		{
			return new ActionProcessingDetailFaultException(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), description, String.format(FAULT_THROWABLE, code, description, reason));
		}
		
		public static ActionProcessingDetailFaultException createFault(final String code, final String description, final Throwable t)
		{
			return new ActionProcessingDetailFaultException(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), description, String.format(FAULT_THROWABLE, code, description, ExceptionUtils.getRootCauseMessage(t)), t);
		}	
	}
	
}
