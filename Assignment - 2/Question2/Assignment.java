package bigdata.bank;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Assignment extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		System.out.println(Arrays.toString(args));
		Job job = Job.getInstance(getConf(), "Assignment1");
		job.setJarByClass(Assignment.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setMapperClass(Map.class);
        job.setCombinerClass(Combine.class);
		job.setReducerClass(Reduce.class);
		//Partitioner Class
		job.setPartitionerClass(JobPartitioner.class);
		//Number of Reduce files determined through total number of job categories
		job.setNumReduceTasks(12);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.waitForCompletion(true);
	      
		return 0;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(Arrays.toString(args));
		int res = ToolRunner.run(new Configuration(), new Assignment(), args);   
		System.exit(res);	
		}
	
	//Mapper Class
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
      
      @Override
      public void map(LongWritable key, Text value, Context context)
    		  throws IOException, InterruptedException, NumberFormatException {
         String line[] = value.toString().split(";");
         Text jobCategory = new Text(line[1].substring(1, line[1].length() - 1));
         Text age = new Text(line[0]);
         Text balance = new Text(line[5]);
         if (balance.toString().equals("\"balance\"")) {
        	 //System.out.println("Header Check");
         } 
         else {
        	 Text output = new Text("Age = " + age + " Balance = " + balance );
        	 context.write(jobCategory, output);
         }	   
      }
	}
	
    //Reducer Class
	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			
			for (Text value : values) {
				context.write(key, value);
			}
		}
	}
	
	//Combiner Class
	public static class Combine extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			
			for (Text value : values) {
				context.write(key, value);
			}
		}
	}
	
	//Partitioner Class
	public static class JobPartitioner extends Partitioner<Text, Text > {
		@Override
		public int getPartition(Text key, Text value, int numPartitions) {
			if(key.toString().equals("admin")) {
				return 0;
			}
			else if(key.toString().equals("unknown")) {
				return 1 % numPartitions;
			}
			else if(key.toString().equals("unemployed")) {
				return 2 % numPartitions;
			}
			else if(key.toString().equals("management")) {
				return 3 % numPartitions;
			}
			else if(key.toString().equals("housemaid")) {
				return 4 % numPartitions;
			}
			else if(key.toString().equals("entrepreneur")) {
				return 5 % numPartitions;
			}
			else if(key.toString().equals("student")) {
				return 6 % numPartitions;
			}
			else if(key.toString().equals("blue-collar")) {
				return 7 % numPartitions;
			}
			else if(key.toString().equals("self-employed")) {
				return 8 % numPartitions;
			}
			else if(key.toString().equals("retired")) {
				return 9 % numPartitions;
			}
			else if(key.toString().equals("technician")) {
				return 10 % numPartitions;
			}
			else { // Services Category
				return 11 % numPartitions;
			}
		}
	}
	

}
