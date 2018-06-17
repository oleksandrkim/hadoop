package com.mop.weather;
import org.apache.hadoop.conf.Configured;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
//import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
//import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import com.mop.weather.weatherDriver;
import com.mop.weather.weatherMapper;
import com.mop.weather.weatherReducer;


public class weatherDriver extends Configured implements Tool {


	
	@Override
	public int run(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.err.println("Usage: fberature <input path> <output path>");
			System.exit(-1);
		}

		//Job Setup
		Job fb = Job.getInstance(getConf(), "facebook-friends");
		
		fb.setJarByClass(weatherDriver.class);
		
		
		//File Input and Output format
		FileInputFormat.addInputPath(fb, new Path(args[0]));
		FileOutputFormat.setOutputPath(fb, new Path(args[1]));
		
		fb.setInputFormatClass(TextInputFormat.class);
		fb.setOutputFormatClass(SequenceFileOutputFormat.class);

		//Output types
		
				
		fb.setMapperClass(weatherMapper.class);
		fb.setReducerClass(weatherReducer.class);
		
		fb.setOutputKeyClass(IntWritable.class); //type of a key (stock code)
		fb.setOutputValueClass(DoubleWritable.class); //type of a value (price); 

		
		//Submit job
		return fb.waitForCompletion(true) ? 0 : 1;
		
	}

	public static void main(String[] args) throws Exception {

		int exitCode = ToolRunner.run(new weatherDriver(), args);
		System.exit(exitCode);
	}}
