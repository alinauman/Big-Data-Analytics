package bigdata.keypair;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.LongSumReducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;



public class KeyPairDriver extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {

    if (args.length != 2) {
      System.out.printf("Usage: " + this.getClass().getName() + " <input dir> <output dir>\n");
      return -1;
    }

    Job job = Job.getInstance(getConf());
    job.setJarByClass(KeyPairDriver.class);
    job.setJobName("Custom Writable Comparable");
    
 
    FileSystem fs = FileSystem.get(new Configuration());
    // true stands for recursively deleting the folder you gave
    fs.delete(new Path("output"), true);
    

    FileInputFormat.setInputPaths(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));


    
    job.setReducerClass(KeyPairReducer.class);
    job.setMapperClass(KeyPairMapper.class);
    job.setPartitionerClass(KeyPairPartitioner.class);
/************* PRovide implementation for all three classes KeyPairReducer, KeyPairMapper aand KeyPairPartitioner***********/
    
	/*
	 * Set the key output class for the job
	 */   
    job.setOutputKeyClass(KeyPairWritable.class);
    
    /*
     * Set the value output class for the job
     */
    job.setOutputValueClass(IntWritable.class);
    
    job.setNumReduceTasks(3);

    boolean success = job.waitForCompletion(true);
    return success ? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
    int exitCode = ToolRunner.run(new Configuration(), new KeyPairDriver(), args);
    System.exit(exitCode);
  }
  
  public static class KeyPairPartitioner extends Partitioner<KeyPairWritable, IntWritable> {
      @Override
      public int getPartition(KeyPairWritable key, IntWritable value, int numPartitions) {
            // you code goes here         // return the appropriate value
    	 
    	  
    	  
     }
}
  
  public static class KeyPairReducer extends Reducer<...think about the parameters reducer should recieve and reducer should output> {
      
/***** create variable over here to hold the value across the different reduce function call******/
     float Total = 0;
     int CurrentKey =0; 
    		 
      @Override
      public void reduce(KeyPairWritable key, Iterable<IntWritable> values, Context context)
              throws IOException, InterruptedException {
    	  
    // you code goes here         


         
      }
   }
}




