package bigdata.bank;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Assignment extends Configured implements Tool {
	public static void main(String[] args)
	throws Exception {
		System.out.println(Arrays.toString(args));
		int res = ToolRunner.run(new Configuration(), new Assignment(), args);
	      
		System.exit(res);
	}

	@Override
	public int run(String[] args)throws Exception {
		System.out.println(Arrays.toString(args));
		Job job = Job.getInstance(getConf(), "Quest1");
		job.setJarByClass(Assignment.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		
		//Set Classes
		job.setMapperClass(Map.class);
        job.setCombinerClass(Combine.class);
		job.setReducerClass(Reduce.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.waitForCompletion(true);
	      
		return 0;
	}

	
	// Mapper Class
	public static class Map extends Mapper<LongWritable, Text, IntWritable, Text> {
      private MapWritable maxBal;
      private MapWritable minBal;
      private MapWritable sumBal;
      private MapWritable count;
      
      
      @Override 
      public void setup(Context context)
	  throws IOException, InterruptedException  {
    	  minBal = new MapWritable();
    	  maxBal = new MapWritable();
    	  sumBal = new MapWritable();
    	  count = new MapWritable();
      }
      
      @Override
      public void map(LongWritable key, Text value, Context context)
	  throws IOException, InterruptedException, NumberFormatException {        
         String line[] = value.toString().split(";");
         Text age = new Text(line[0]);
         Text balance = new Text(line[5]);

         if (balance.toString().equals("\"balance\"")) {
         } 
         else {
	         int balanceNew = Integer.parseInt(line[5]);
	         // Maximum Conditions
	         if (maxBal.get(age) == null) {
	        	 maxBal.put(age, new IntWritable(Integer.parseInt(balance.toString())));
	        	 sumBal.put(age, new IntWritable(Integer.parseInt(balance.toString())));
	        	 count.put(age, new IntWritable(1));
	         } 
	         else if (Integer.parseInt(maxBal.get(age).toString()) < balanceNew) {
	        	 maxBal.put(age, new IntWritable(Integer.parseInt(balance.toString())));	 
	        	 int countInt = Integer.parseInt(count.get(age).toString());
	        	 countInt++;
	        	 count.put(age, new IntWritable(countInt));
	        	 int balSumInt = Integer.parseInt(sumBal.get(age).toString());
	        	 balSumInt += balanceNew;
	        	 sumBal.put(age, new IntWritable(balSumInt));
	         }
	         // Minimum Conditions
	         if (minBal.get(age) == null) {
	        	 minBal.put(age, new IntWritable(Integer.parseInt(balance.toString())));
	         } 
	         else if (Integer.parseInt(minBal.get(age).toString())  > balanceNew) {
	        	 minBal.put(age, new IntWritable(Integer.parseInt(balance.toString())));
	        	 int countInt = Integer.parseInt(count.get(age).toString());
	        	 countInt++;
	        	 count.put(age, new IntWritable(countInt));
	        	 int balSumInt = Integer.parseInt(sumBal.get(age).toString());
	        	 balSumInt += balanceNew;
	        	 sumBal.put(age, new IntWritable(balSumInt));
	         }
         }
    	   
      }
      
      @Override 
      public void cleanup(Context context) throws IOException, InterruptedException  {
    	  for (Writable value: maxBal.keySet()) {
    		  int sum = Integer.parseInt(sumBal.get(value).toString());
    		  int max = Integer.parseInt(maxBal.get(value).toString());
    		  int min = Integer.parseInt(minBal.get(value).toString());
    		  int num = Integer.parseInt(count.get(value).toString());
    		  Text outNew = new Text(String.valueOf(sum) + " " + String.valueOf(max) + " " + String.valueOf(min) + " " + String.valueOf(num));

    		  context.write(new IntWritable(Integer.parseInt(value.toString())), outNew);
    	  }
      }
	}
    // Reducer Class
	public static class Reduce extends Reducer<IntWritable, Text, IntWritable, Text> {
		@Override
		public void reduce(IntWritable key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			int count = 0;
			int maximum = 0;
			int minimum = 0;
			for (Text value : values) {
				String a[] = value.toString().split(" ");
				if(count==0){
					minimum = Integer.parseInt(a[2]);
				}
				sum += Integer.parseInt(a[0]);
				count += Integer.parseInt(a[3]);
				if (maximum < Integer.parseInt(a[1])) {
					maximum = Integer.parseInt(a[1]);
				}
				if (minimum > Integer.parseInt(a[2])) {
					minimum = Integer.parseInt(a[2]);
				}
	         }
			
			int average = sum / count;
			int s = Integer.parseInt(key.toString());
			Text output = new Text("Age = " + String.valueOf(s) + " , MaxBalance =  " + String.valueOf(maximum) + 
					" , MinBalance = " + String.valueOf(minimum) + " , AvgBalance = " + 
			String.valueOf(average));
			
			context.write(key, output);			
		}
	}
	// Combiner Class
	public static class Combine extends Reducer<IntWritable, Text, IntWritable, Text> {
		@Override
		public void reduce(IntWritable key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			int count = 0;
			int maximum = 0;
			int minimum = 0;
			for (Text value : values) {
				String a[] = value.toString().split(" ");
				if(count==0){
					minimum = Integer.parseInt(a[2]);
				}
				sum += Integer.parseInt(a[0]);
				count += Integer.parseInt(a[3]);
				if (maximum < Integer.parseInt(a[1])) {
					maximum = Integer.parseInt(a[1]);
				}
				if (minimum > Integer.parseInt(a[2])) {
					minimum = Integer.parseInt(a[2]);
				}
	         }
			
			Text output = new Text(String.valueOf(sum) + " " + String.valueOf(maximum) + " " + String.valueOf(minimum) + " " + String.valueOf(count));
			
			context.write(key, output);			
		}
		
	}
	

}

