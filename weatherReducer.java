package com.mop.weather;

import java.io.IOException;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.apache.hadoop.io.DoubleWritable;
//import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
//import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class weatherReducer extends Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable> {
	//first two define the input to a reducer - Text, FloatWritable
	//two others - the output from the reducer (Text, FloatWritable)
	
	 @Override
	 public void reduce(IntWritable key, Iterable<DoubleWritable> values, Context context)
			 throws IOException, InterruptedException {
		 
		 //key - month; values - iterable list of percentage difference stocks; 
		 
		 double sum_temp = Double.MIN_VALUE; //puts the smallest value possible?;
		 int elementNumb = 0; 
		 
		 //Iterate all closing prices and calculate maximum
		 for (DoubleWritable value : values) {
			 sum_temp = sum_temp + value.get(); //get allows using DoubleWritable as double
			 elementNumb += 1;
		 }
		 
		 double aver_temp = sum_temp/elementNumb;
		 
		 //Write output
		 context.write(key, new DoubleWritable(aver_temp));
	 }
	
}
