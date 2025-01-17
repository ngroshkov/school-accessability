package com.school.proximity;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class MainCompile {
	private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


	private static final int SCHOOL_ID = MainDownload.SCHOOL_ID;
	private static final int[] SCHOOL_OBJECTIDS = MainDownload.SCHOOL_OBJECTIDS;
	private static final String MODE = MainDownload.MODE;
	private static final String HOUSES_INPUT_FILE_NAME = MainDownload.HOUSES_INPUT_FILE_NAME;
	
	
	
	public static void main(String[] args) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		DocumentBuilder builder = factory.newDocumentBuilder();
		
		CSVWriter writer;
		String[] entries = new String[SCHOOL_OBJECTIDS.length * 2 + 1];
		writer = new CSVWriter(new FileWriter(String.format("houses_%s_%s.csv", MODE, SCHOOL_ID)), ';');
		
		entries[0] = "ID";
		
		if (SCHOOL_OBJECTIDS.length != 1) {
			for (int i = 1; i < entries.length; i++) {
				String type = (i-1)%2 == 0 ? "DURATION" : "DISTANCE";
				entries[i] = MODE.toUpperCase() + "_" + type + "_" + SCHOOL_OBJECTIDS[(i-1)/2];
			}
		} else {
			entries[1] = MODE.toUpperCase() + "_DURATION";
			entries[2] = MODE.toUpperCase() + "_DISTANCE";
		}
		
	    writer.writeNext(entries);
	    
	    
	    CSVReader housesReader = new CSVReader(new FileReader(HOUSES_INPUT_FILE_NAME + ".csv"), ';');
		String[] houseFirstLine = housesReader.readNext();
		int idHouseIndex = fieldIndex("ID", houseFirstLine);
		
		
		String [] nextLine;

		// ----------------------------------
		XPathExpression elementExpression = xpath.compile("/DistanceMatrixResponse/row/element");
		XPathExpression durationExpression = xpath.compile("duration/value");
		XPathExpression distanceExpression = xpath.compile("distance/value");

		InputStream[] xmlInputStream = new InputStream[SCHOOL_OBJECTIDS.length];
		Document[] document = new Document[SCHOOL_OBJECTIDS.length];
		NodeList[] elementList = new NodeList[SCHOOL_OBJECTIDS.length];
		int elementListIndex = 0;
		int fileID = -1;
		
	    while ((nextLine = housesReader.readNext()) != null) {
	    	String[] entry = new String[SCHOOL_OBJECTIDS.length * 2 + 1];
	    	int id = Integer.valueOf(nextLine[idHouseIndex]);
	    	entry[0] = String.valueOf(id);

	    	
			if (elementList[0] == null || elementListIndex >= elementList[0].getLength()) {
				++fileID;
				elementListIndex = 0;
				if (SCHOOL_OBJECTIDS.length == 1) {
					xmlInputStream[0] = new FileInputStream(String.format("response/houses_%s_%s[%s].xml", MODE, SCHOOL_ID, fileID));
				} else {
					for (int i = 0 ; i < SCHOOL_OBJECTIDS.length; i++) {
						xmlInputStream[i] = new FileInputStream(String.format("response/houses_%s_%s[%s]-[%s].xml", MODE, SCHOOL_ID, fileID, SCHOOL_OBJECTIDS[i]));
					}
				}
				
				for (int i = 0 ; i < SCHOOL_OBJECTIDS.length; i++) {
					document[i] = builder.parse(xmlInputStream[i]);
					elementList[i] = (NodeList) elementExpression.evaluate(document[i], XPathConstants.NODESET);
				}
			}
			
			
			for (int i = 0 ; i < SCHOOL_OBJECTIDS.length; i++) {
				Node node = elementList[i].item(elementListIndex);
			
				System.out.println(fileID + " " + elementListIndex);
				try {
					String duration = (String) durationExpression.evaluate(node, XPathConstants.STRING);
					String distance = (String) distanceExpression.evaluate(node, XPathConstants.STRING);
			
					entry[(2*i) + 1] = duration;
					entry[(2*i) + 2] = distance;
					
				} catch (XPathExpressionException  e){
					writer.close();
					return;
				}
			}
			
			writer.writeNext(entry);
			
			++elementListIndex;
//	    	System.out.println(id + " " + x + " " + y);	
	    }

	    
	    
		writer.close();
		
	}
	
	private static int fieldIndex(String fieldName, String[] firstLine) {
		for (int i = 0; i < firstLine.length; i++) {
			if (fieldName.equals(firstLine[i])) {
				return i;
			}
		}
		throw new UnsupportedOperationException("Not found!");
	}


}
