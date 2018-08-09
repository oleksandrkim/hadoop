### Calculate average temperature across months. Small example of how to extract data from json into mapreduce
Data stored in **weather**<br/>
The **mapreduce output**can be found in mapreduce_output.txt <br/>
The **log** file of the mapreduce - mapreduce_log.txt<br/>
The **jar** file used to run a mapreduce job - weather.jar<br/>
Driver, Mapper and Reducer are saved as separate files for a reference<br/>
The code to **extract data from API** is in weather_extraction.py

**Driver**

```
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
```
<br>
   
**Mapper**

```
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
```
<br>
  
**Reducer**

```
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
```
<br>
  
**Log**

```hirwuser864@ip-172-31-45-217:~$ hadoop jar /home/hirwuser864/weather/weather.jar com.mop.weather.weatherDriver -libjars /hirw-workshop/mapreduce/facebook/json-simple-1.1.jar /user/hirwuser864/weather_input/input/ /user/hirwuser864/weather_output
18/06/13 19:54:25 INFO client.RMProxy: Connecting to ResourceManager at ip-172-31-45-216.ec2.internal/172.31.45.216:8032
18/06/13 19:54:26 INFO input.FileInputFormat: Total input paths to process : 1
18/06/13 19:54:26 INFO mapreduce.JobSubmitter: number of splits:1
18/06/13 19:54:26 INFO mapreduce.JobSubmitter: Submitting tokens for job: job_1525967314796_1006
18/06/13 19:54:26 INFO impl.YarnClientImpl: Submitted application application_1525967314796_1006
18/06/13 19:54:26 INFO mapreduce.Job: The url to track the job: http://ec2-54-92-244-237.compute-1.amazonaws.com:8088/proxy/application_1525967314796_1006/
18/06/13 19:54:26 INFO mapreduce.Job: Running job: job_1525967314796_1006
18/06/13 19:54:32 INFO mapreduce.Job: Job job_1525967314796_1006 running in uber mode : false
18/06/13 19:54:32 INFO mapreduce.Job:  map 0% reduce 0%
18/06/13 19:54:37 INFO mapreduce.Job:  map 100% reduce 0%
18/06/13 19:54:43 INFO mapreduce.Job:  map 100% reduce 100%
18/06/13 19:54:43 INFO mapreduce.Job: Job job_1525967314796_1006 completed successfully
18/06/13 19:54:43 INFO mapreduce.Job: Counters: 53
        File System Counters
                FILE: Number of bytes read=10226
                FILE: Number of bytes written=271177
                FILE: Number of read operations=0
                FILE: Number of large read operations=0
                FILE: Number of write operations=0
                HDFS: Number of bytes read=762281
                HDFS: Number of bytes written=335
                HDFS: Number of read operations=6
                HDFS: Number of large read operations=0
                HDFS: Number of write operations=2
        Job Counters
                Launched map tasks=1
                Launched reduce tasks=1
                Data-local map tasks=1
                Total time spent by all maps in occupied slots (ms)=14404
                Total time spent by all reduces in occupied slots (ms)=12536
                Total time spent by all map tasks (ms)=3601
                Total time spent by all reduce tasks (ms)=3134
                Total vcore-milliseconds taken by all map tasks=3601
                Total vcore-milliseconds taken by all reduce tasks=3134
                Total megabyte-milliseconds taken by all map tasks=3687424
                Total megabyte-milliseconds taken by all reduce tasks=3209216
        Map-Reduce Framework
                Map input records=730
                Map output records=730
                Map output bytes=8760
                Map output materialized bytes=10226
                Input split bytes=151
                Combine input records=0
                Combine output records=0
                Reduce input groups=12
                Reduce shuffle bytes=10226
                Reduce input records=730
                Reduce output records=12
                Spilled Records=1460
                Shuffled Maps =1
                Failed Shuffles=0
                Merged Map outputs=1
                GC time elapsed (ms)=169
                CPU time spent (ms)=1660
                Physical memory (bytes) snapshot=700690432
                Virtual memory (bytes) snapshot=2772324352
                Total committed heap usage (bytes)=579338240
                Peak Map Physical memory (bytes)=498581504
                Peak Map Virtual memory (bytes)=1383337984
                Peak Reduce Physical memory (bytes)=202108928
                Peak Reduce Virtual memory (bytes)=1388986368
        Shuffle Errors
                BAD_ID=0
                CONNECTION=0
                IO_ERROR=0
                WRONG_LENGTH=0
                WRONG_MAP=0
                WRONG_REDUCE=0
        File Input Format Counters
                Bytes Read=762130
        File Output Format Counters
                Bytes Written=335
```
<br>
  
## Output
>1       2.0919354838709685 <br>
>2       7.350701754385967 <br>
>3       10.817741935483872 <br>
>4       13.178666666666668 <br>
>5       19.28870967741935 <br>
>6       23.587166666666672 <br>
>7       24.801774193548386 <br>
>8       24.490645161290313 <br>
>9       19.6145 <br>
>10      14.002096774193552 <br>
>11      7.229833333333333 <br>
>12      3.977213114754097 
