package com.mop.weather;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.Locale;

import org.apache.hadoop.io.DoubleWritable;
//import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
//import org.apache.hadoop.mapreduce.Mapper.Context;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import com.hirw.maxcloseprice.MaxClosePriceMapper.Volume;


public class weatherMapper extends Mapper<LongWritable, Text, IntWritable, DoubleWritable> {

	double init = 184.65; //price of a stock in the end of 2014;
	@Override //we need to overwrite "map" method;
	public void map(LongWritable key, Text value, Context context) 
			throws IOException, InterruptedException {

		
		
		String line = value.toString(); //convert the value (record) to a string;
		
		try {
			JSONParser parser = new JSONParser();
            Object obj = parser.parse(line);
 
            JSONObject jsonObject = (JSONObject) obj;
 
            long time = (long) jsonObject.get("time");
            //double l = jsonObject.get("temperatureHigh");
            String tempstring = String.valueOf(jsonObject.get("temperatureHigh"));
            double temperature = Double.valueOf(tempstring);
            
            Date date = new java.util.Date((long)time*1000L); 
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy"); 
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+2")); 
            String formattedDate = sdf.format(date);
            String subs = formattedDate.substring(3, 5);
            int month = Integer.parseInt(subs);
    		context.write(new IntWritable(month), new DoubleWritable(temperature)); //used to submit a mapper output;

 
		
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

		
	}
	
}
