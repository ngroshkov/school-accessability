package com.school.proximity;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import au.com.bytecode.opencsv.CSVReader;

public class MainDownload {
	
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.FRANCE);
	
	private static final String DRIVING_MODE = "driving";
	private static final String WALKING_MODE = "walking";

	
	public static final String MODE = WALKING_MODE;

	/* SETTINGS !!! */
	public static final int SCHOOL_ID = 1062; // 
	public static final int[] SCHOOL_OBJECTIDS = new int[]{1}; // 
	public static final String SCHOOLS_INPUT_FILE_NAME = "schools_butovo_xy"; // 
	public static final String HOUSES_INPUT_FILE_NAME = "houses_butovo_xy"; // 

	
	
	private static final DefaultHttpClient httpclient = new DefaultHttpClient();
	
	
	private static final String API_KEY = "AIzaSyBxNZDQLHR5w02ksMtYSn3y5fwt-yxRSjo";
	




	public static void main(String[] args) throws ClientProtocolException, IOException, ParseException, InterruptedException, URISyntaxException {
		
		CSVReader schoolReader = new CSVReader(new FileReader(SCHOOLS_INPUT_FILE_NAME + ".csv"), ';');
		CSVReader housesReader = new CSVReader(new FileReader(HOUSES_INPUT_FILE_NAME + ".csv"), ';');
		
		String[] schoolFirstLine = schoolReader.readNext();
		int objectIdSchoolIndex = fieldIndex("OBJECTID", schoolFirstLine);
		int idSchoolIndex = fieldIndex("ID", schoolFirstLine);
		int xSchoolIndex = fieldIndex("POINT_X", schoolFirstLine);
		int ySchoolIndex= fieldIndex("POINT_Y", schoolFirstLine);
		
		String[] houseFirstLine = housesReader.readNext();
		int idHouseIndex = fieldIndex("ID", houseFirstLine);
		int xHouseIndex = fieldIndex("POINT_X", houseFirstLine);
		int yHouseIndex= fieldIndex("POINT_Y", houseFirstLine);

		
	    String [] nextLine;
	    


	    int[] idSchools = new int[SCHOOL_OBJECTIDS.length];
		double[] xSchools = new double[SCHOOL_OBJECTIDS.length];
		double[] ySchools = new double[SCHOOL_OBJECTIDS.length];

		int i = 0;
	    while ((nextLine = schoolReader.readNext()) != null) {
	    	if (Arrays.binarySearch(SCHOOL_OBJECTIDS, Integer.valueOf(nextLine[objectIdSchoolIndex])) >= 0) {
	    		idSchools[i] = Integer.valueOf(nextLine[idSchoolIndex]);
	    		xSchools[i] = NUMBER_FORMAT.parse(nextLine[xSchoolIndex]).doubleValue();
	    		ySchools[i] = NUMBER_FORMAT.parse(nextLine[ySchoolIndex]).doubleValue();
	    		i++;
	    	}
	    }
	    
			


	    

	    List<Integer> houseIDs = new ArrayList<Integer>();
	    List<String> houseCoordinates = new ArrayList<String>();
	    
	    
	    
	    
		for (i = 0; i < idSchools.length; i++) {
			int idSchool = idSchools[i];
			double xSchool = xSchools[i];
			double ySchool = ySchools[i];
			
			System.out.println(idSchool + " " + xSchool + " " + ySchool);

			while ((nextLine = housesReader.readNext()) != null) {
				int idHouse = Integer.valueOf(nextLine[idHouseIndex]);
				double xHouse = NUMBER_FORMAT.parse(nextLine[xHouseIndex]).doubleValue();
				double yHouse = NUMBER_FORMAT.parse(nextLine[yHouseIndex]).doubleValue();

				houseIDs.add(idHouse);
				houseCoordinates.add(yHouse + "," + xHouse);
				// System.out.println(id + " " + x + " " + y);
			}


			// ------------------------------------------------------------------

			URIBuilder builder = new URIBuilder();
			builder.setScheme("https").setHost("maps.googleapis.com")
					.setPath("/maps/api/distancematrix/xml")
					.setParameter("origins", ySchool + "," + xSchool)
					.setParameter("mode", MODE).setParameter("sensor", "false");
			// .setParameter("key", API_KEY);

			int count = 0;
			int fileNumb = 0;
			List<String> houseCoordinatesPart = new ArrayList<String>();
			for (int it = 0; it < houseCoordinates.size(); it++) {

				houseCoordinatesPart.add(houseCoordinates.get(it));
				++count;

				if (count == 50	|| (count < 50 && it == houseCoordinates.size() - 1)) {
					String destinations = StringUtils.join(houseCoordinatesPart.toArray(), "|");

					// query
					builder.setParameter("destinations", destinations);

					URI uri = builder.build();
					HttpGet httpget = new HttpGet(uri);
					

					System.out.println(httpget.getURI());
					HttpResponse response = httpclient.execute(httpget);

					OutputStream output;
					if (SCHOOL_OBJECTIDS.length == 1) {
						output = new FileOutputStream(String.format("response/houses_%s_%s[%s].xml", MODE, idSchool, fileNumb));
					} else {
						output = new FileOutputStream(String.format("response/houses_%s_%s[%s]-[%s].xml", MODE,	idSchool, fileNumb, SCHOOL_OBJECTIDS[i]));
					}
					response.getEntity().writeTo(output);
					houseCoordinatesPart = new ArrayList<String>();
					count = 0;
					++fileNumb;

					Thread.sleep(6000);
//					 Thread.sleep(1000);
				}

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
