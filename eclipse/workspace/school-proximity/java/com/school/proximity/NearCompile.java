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

public class NearCompile {
	
	private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	private static final String MODE = MainDownload.MODE;
	
	private static final String RESULT_FILE_NAME = "buildings_near_schools_result[4]";

//	buildings_near_schools_result[5]
//	buildings_near_schools_result[6]
//	buildings_near_schools_result[7]
//	buildings_near_schools_result[8]
//	buildings_near_schools_result[9]
//	buildings_near_schools_result[10]
//	buildings_near_schools_result[11]
//	buildings_near_schools_result[12]
//	buildings_near_schools_result[13]
//	buildings_near_schools_result[14]
//	buildings_near_schools_result[15]
//	buildings_near_schools_result[16]


	
	private static final String HOUSES_INPUT_FILE_NAME = NearDownload.HOUSES_INPUT_FILE_NAME;
	
	public static void main(String[] args) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		DocumentBuilder builder = factory.newDocumentBuilder();
		
		XPathExpression elementExpression = xpath.compile("/DistanceMatrixResponse/row/element");
		XPathExpression durationExpression = xpath.compile("duration/value");
		XPathExpression distanceExpression = xpath.compile("distance/value");

		
		CSVReader housesReader = new CSVReader(new FileReader(HOUSES_INPUT_FILE_NAME + ".csv"), ';');
		String[] houseFirstLine = housesReader.readNext();
		int idHouseIndex = NearDownload.fieldIndex("ID", houseFirstLine);


		CSVWriter writer = new CSVWriter(new FileWriter(String.format(RESULT_FILE_NAME + ".csv")), ';');

		String[] entries = new String[3];
		entries[0] = "ID";
		entries[1] = MODE.toUpperCase() + "_NEAR_DURATION";
		entries[2] = MODE.toUpperCase() + "_NEAR_DISTANCE";

		writer.writeNext(entries);
		
		String [] nextLine;
		while ((nextLine = housesReader.readNext()) != null) {
			int idHouse = Integer.valueOf(nextLine[idHouseIndex]);

			InputStream xmlInputStream = new FileInputStream(String.format("response/houses_near_%s.xml", idHouse));
			
			Document document = builder.parse(xmlInputStream);
			NodeList elementList = (NodeList) elementExpression.evaluate(document, XPathConstants.NODESET);
			Node node = elementList.item(0);
			System.out.println(idHouse);
			String duration = (String) durationExpression.evaluate(node, XPathConstants.STRING);
			String distance = (String) distanceExpression.evaluate(node, XPathConstants.STRING);

			String[] entry = new String[3];
			entry[0] = String.valueOf(idHouse);
			entry[1] = duration;
			entry[2] = distance;

			writer.writeNext(entry);
		}
		
		writer.close();
		System.out.println("Compiling has been finished!!!");
	}

}
