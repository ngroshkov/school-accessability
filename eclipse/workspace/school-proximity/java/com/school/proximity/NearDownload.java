package com.school.proximity;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import au.com.bytecode.opencsv.CSVReader;

public class NearDownload {
	
//	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.FRANCE);
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
	
	private static final String DRIVING_MODE = "driving";
	private static final String WALKING_MODE = "walking";

	public static final String MODE = WALKING_MODE;
	
	public static final String HOUSES_INPUT_FILE_NAME = "buildings_near_schools[4]";

//	buildings_near_schools[5]
//	buildings_near_schools[6]
//	buildings_near_schools[7]
//	buildings_near_schools[8]
//	buildings_near_schools[9]
//	buildings_near_schools[10]
//	buildings_near_schools[11]
//	buildings_near_schools[12]
//	buildings_near_schools[13]
//	buildings_near_schools[14]
//	buildings_near_schools[15]
//	buildings_near_schools[16]

	
	private static final DefaultHttpClient httpclient = new DefaultHttpClient();

	public static void main(String[] args) throws IOException, ParseException, URISyntaxException, InterruptedException {
		
		
		CSVReader housesReader = new CSVReader(new FileReader(HOUSES_INPUT_FILE_NAME + ".csv"), ';');

		String[] houseFirstLine = housesReader.readNext();
		int idHouseIndex = fieldIndex("ID", houseFirstLine);
		int xHouseIndex = fieldIndex("POINT_X", houseFirstLine);
		int yHouseIndex= fieldIndex("POINT_Y", houseFirstLine);
		
		int xNearIndex = fieldIndex("NEAR_X", houseFirstLine);
		int yNearIndex= fieldIndex("NEAR_Y", houseFirstLine);

		
		String [] nextLine;
		
		int count = 0;
		int iterator = 1;
		while ((nextLine = housesReader.readNext()) != null) {
			int idHouse = Integer.valueOf(nextLine[idHouseIndex]);
			double xHouse = NUMBER_FORMAT.parse(nextLine[xHouseIndex]).doubleValue();
			double yHouse = NUMBER_FORMAT.parse(nextLine[yHouseIndex]).doubleValue();
			
			double xNear = NUMBER_FORMAT.parse(nextLine[xNearIndex]).doubleValue();
			double yNear = NUMBER_FORMAT.parse(nextLine[yNearIndex]).doubleValue();
			
			
			URIBuilder builder = new URIBuilder();
			builder.setScheme("https").setHost("maps.googleapis.com")
					.setPath("/maps/api/distancematrix/xml")
					.setParameter("origins", yHouse + "," + xHouse)
					.setParameter("destinations", yNear + "," + xNear)
					.setParameter("mode", MODE).setParameter("sensor", "false");
			
			URI uri = builder.build();
			HttpGet httpget = new HttpGet(uri);
			System.out.println(iterator + " " + httpget.getURI());
			HttpResponse response = httpclient.execute(httpget);

			OutputStream output = new FileOutputStream(String.format("response/houses_near_%s.xml", idHouse));
			response.getEntity().writeTo(output);
			
			iterator++;
			count++;
			if (count >= 50) {
				Thread.sleep(6000);
				count = 0;
			}
		}


	}
	
	public static int fieldIndex(String fieldName, String[] firstLine) {
		for (int i = 0; i < firstLine.length; i++) {
			if (fieldName.equals(firstLine[i])) {
				return i;
			}
		}
		throw new UnsupportedOperationException("Not found!");
	}
	


}
