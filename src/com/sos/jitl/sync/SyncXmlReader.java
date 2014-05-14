package com.sos.jitl.sync;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

public class SyncXmlReader {

	private final String		xml;
	private final String		xPathExpression;
	private NodeList	nodes;
	private int			index;
	private SOSXMLXPath	xPath;
	private Node		lastNode;

	public SyncXmlReader(final String xml, final String xPathExpression) throws Exception {
		super();
		this.xml = xml;
		this.xPathExpression = xPathExpression;
		getNodes();
	}

	private void getNodes() throws Exception {
		xPath = new SOSXMLXPath(new StringBuffer(xml));
		xPath.
		nodes = xPath.selectNodeList(xPathExpression);
		index = 0;
	}

	public boolean eof() {
		return index >= nodes.getLength();
	}

	public void getNext() {
		lastNode = nodes.item(index);
		index++;
	}

	public String getAttributeValue(final String attribute) {
		String strR = "";
		if (lastNode != null) {
			Node objN = lastNode.getAttributes().getNamedItem(attribute);
			if (objN != null) {
				strR = objN.getNodeValue();
			}
			return strR;
		}
		else {
			return strR;
		}
	}

	public String getAttributeValueFromParent(final String attribute) {
		if (lastNode != null) {
			return lastNode.getParentNode().getAttributes().getNamedItem(attribute).getNodeValue();
		}
		else {
			return null;
		}

	}

	public Node getNode(final String xpression) throws Exception {
		return xPath.selectSingleNode(xpression);
	}

	public String getAttributeValueFromXpath(final String xPath, final String attribute) throws Exception {
		Node n = getNode(xPath);
		if (n == null) {
			return "";
		}
		else {
			return n.getAttributes().getNamedItem(attribute).getNodeValue();
		}

	}

}
