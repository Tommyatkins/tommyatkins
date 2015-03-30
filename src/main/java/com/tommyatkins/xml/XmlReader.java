package com.tommyatkins.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

public class XmlReader extends StAXOMBuilder {

	private XmlReader(InputStream is) throws XMLStreamException {
		super(is);
	}

	private XmlReader(String content, String encoding) throws UnsupportedEncodingException, XMLStreamException {
		this(new ByteArrayInputStream(content.getBytes(encoding)));
	}

	@SuppressWarnings("unchecked")
	public OMElement getChildEleByParentEle(OMElement ele, String childName) {
		if (null == ele || null == childName)
			return null;
		Iterator<OMElement> it = ele.getChildElements();
		OMElement o = null;
		while (it.hasNext()) {
			o = it.next();
			if (o.getLocalName().equals(childName))
				break;
		}
		return o;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<OMElement> getChildren(OMElement ele) {
		return toCollection(ele.getChildElements());
	}

	private Collection<OMElement> toCollection(Iterator<OMElement> childElements) {
		List<OMElement> list = new ArrayList<OMElement>();
		while (childElements.hasNext()) {
			list.add(childElements.next());
		}
		return list;
	}

	public static XmlReader buildXmlDoc(String xmlContent, String encoding) {
		XmlReader reader = null;
		ByteArrayInputStream bias = null;
		try {
			bias = new ByteArrayInputStream(xmlContent.getBytes(encoding));
			reader = new XmlReader(bias);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bias != null) {
					bias.close();
				}
			} catch (IOException e) {

			}

		}
		return reader;
	}

}
