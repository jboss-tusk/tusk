package org.drools.support.xml;

import org.drools.support.MessagePayload;
import org.w3c.dom.Document;

public class XmlMessagePayload extends MessagePayload<Document> {

	public XmlMessagePayload(Document payload) {
		super(payload);
	}

}
