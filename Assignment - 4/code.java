package bigdata.assignment1;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
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
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Assignment1 extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		System.out.println(Arrays.toString(args));
		Job job = Job.getInstance(getConf(), "Assignment1");
		job.setJarByClass(Assignment1.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileSystem fs = FileSystem.get(new Configuration());
		  // true stands for recursively deleting the folder you gave
		fs.delete(new Path("output"), true);
		
		job.setMapperClass(Map.class);
        job.setCombinerClass(Combine.class);
		job.setReducerClass(Reduce.class);
		
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		Configuration conf = new Configuration();
		conf.set("minSup", args[2]);
		conf.set("noBaskets", args[3]);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
//		FileInputFormat.setMaxInputSplitSize(job, (long)Integer.parseInt(args[3]));
		
		job.waitForCompletion(true);
	      
		return 0;
	}

	public static void main(String[] args) throws Exception {
//		System.out.println("Output");
		System.out.println(Arrays.toString(args));
		int res = ToolRunner.run(new Configuration(), new Assignment1(), args);
	      
		System.exit(res);	

	}
	
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {

	private final static IntWritable ONE = new IntWritable(1);	
	private MapWritable candidateList;
		
      @Override
      public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException, NumberFormatException {
         
//    	  System.out.println("Value " + value);
    	  String token[] = value.toString().split(",");
    	  if (token[0].equals("age")) {
    		  return;
    	  }
    	  // Generating single list
    	  for (int i = 0; i < token.length; i++) {
    		  
//    		  System.out.println(token[i].toString());
    		  String k = (i) + token[i].toString();
    		  if (candidateList.get(new Text(k)) == null) {
    			  
    			  candidateList.put(new Text(k), ONE);
    		  } else  {  
 	        	 int c = Integer.parseInt(candidateList.get(new Text(k)).toString());
 	        	 c++;
 	        	 candidateList.put(new Text(k), new IntWritable(c));
    		  }
//    		  context.write(new Text((i) + token[i].toString()), ONE);
//    		  if (i < token.length - 1) {
//    			  context.write(new Text(token[0].toString() + " , "  + token[i + 1].toString()), ONE);  
//    		  }
    	  }
    	 
    	  // Generating pair list
    	  for (int i = 0; i < token.length; i++) {
    		  String pickedAttribute = token[i];
//    		  System.out.println(token[i].toString());
    		  for (int j = i + 1; j < token.length; j++) {
    			  String k = (i) + token[i].toString() + ", "  + (j) + token[j].toString();
        		  if (candidateList.get(new Text(k)) == null) {
        			  candidateList.put(new Text(k), ONE);
        		  } else  {
     	        	 int c = Integer.parseInt(candidateList.get(new Text(k)).toString());
     	        	 c++;
     	        	 candidateList.put(new Text(k), new IntWritable(c));
        		  }
//    			  context.write(new Text((i) + token[i].toString() + ", "  + (j) + token[j].toString()), ONE);
    		  }
//    		  context.write(new Text(token[i].toString()), ONE);
//    		  if (i < token.length - 1) {
//    			  context.write(new Text(token[0].toString() + " , "  + token[i + 1].toString()), ONE);  
//    		  }
    	  }
    	  
    	  // Generating n-pair list
    	  int n = 1;
    	  for (int i = 0; i < token.length; i++) {
    		  n = 1;
    		  for (int j = i + 1; j < token.length; j++) {
    			  String str1 = "";
    			  String str2 = "";
    			  str1 += (i) + token[i];
    			  str1 += ", " + (j) + token[j];
    			  
    			  for (int k = j + 1; k < token.length; k++) {
    				  str1 += ", " +  (k) + token[k];  
    				  String ke = str1;
            		  if (candidateList.get(new Text(ke)) == null) {
            			  candidateList.put(new Text(ke), ONE);
            		  } else  {
         	        	 int c = Integer.parseInt(candidateList.get(new Text(ke)).toString());
         	        	 c++;
         	        	 candidateList.put(new Text(ke), new IntWritable(c));
            		  }
//    				  context.write(new Text(str1), ONE);
    			  }
    			  n++;
    		  } 
    	  }
      }
      
      
      @Override 
      public void setup(Context context) throws IOException, InterruptedException  {
    	  candidateList = new MapWritable();
      }
      
      @Override 
      public void cleanup(Context context) throws IOException, InterruptedException  {
    	  for (Writable val: candidateList.keySet()) {
    		  int count = Integer.parseInt(candidateList.get(new Text(val.toString())).toString());
    		  
    		  Double minSup = Double.parseDouble(context.getConfiguration().get("minSup"));
    		  Integer noBaskets = context.getConfiguration().getInt("noBaskets", 5);
    		  
    		  // Pruning
    		  if (count > (minSup / noBaskets)) {
    			  context.write(new Text(val.toString()), new IntWritable(count));
    		  }
    		   
//    		  context.write(new IntWritable(Integer.parseInt(val.toString())), o); 
    	  }
      }
	}

	public static class Reduce extends Reducer<Text, IntWritable, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			
//			System.out.println("Reduce " + key + " " + values.toString());
			int sum = 0;
			for (IntWritable val : values) {
				int v = Integer.parseInt(val.toString());
				sum += v;
//				
			} 
//			System.out.println(key.toString() + " " + Integer.toString(sum));
			context.write(key,  new Text(Integer.toString(sum)));
		}
	}
	
	public static class Combine extends Reducer<Text, IntWritable, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
			throws IOException, InterruptedException {
			
			int sum = 0;
			for (IntWritable val : values) {
				int v = Integer.parseInt(val.toString());
				sum += v;
//				
			} 
//			System.out.println(key.toString() + " " + Integer.toString(sum));
			context.write(key,  new Text(Integer.toString(sum)));
		}
		
	}
}
