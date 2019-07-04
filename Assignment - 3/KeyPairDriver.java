package bigdata.keypair;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
import org.apache.hadoop.io.WritableComparable;
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

public class KeyPairDriver extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		System.out.println(Arrays.toString(args));
		Job job = Job.getInstance(getConf(), "Assignment1");
		job.setJarByClass(KeyPairDriver.class);
		FileSystem fs = FileSystem.get(new Configuration());
		  // true stands for recursively deleting the folder you gave
		fs.delete(new Path("output"), true);

		job.setPartitionerClass(Partitioner.class);
		job.setMapperClass(Map.class);
        //job.setCombinerClass(Combine.class); // Set Combiner Class.
		job.setReducerClass(Reduce.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setOutputKeyClass(KeyPairWritable.class);
	  	job.setOutputValueClass(IntWritable.class);

		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.waitForCompletion(true);
	      
		return 0;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(Arrays.toString(args));
		int res = ToolRunner.run(new Configuration(), new KeyPairDriver(), args);
		System.exit(res);
	}
	
	public static class Map extends Mapper<LongWritable, Text, KeyPairWritable, IntWritable > {
	
		IntWritable one = new IntWritable(1);
		private KeyPairWritable KeyPair = new KeyPairWritable();
		  
      @Override
      public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException, NumberFormatException {
         
         String line[] = value.toString().split(";");
         Text jobDescription = new Text(line[1].substring(1, line[1].length() - 1)); 
         Text age = new Text(line[0]);
         Text balance = new Text(line[5]);
         
         if (balance.toString().equals("\"balance\"")) {
        	 //System.out.println("If First Line i.e Header Ignore");
         } 
         else {
        	 KeyPair.setAge(Integer.parseInt(age.toString()));
        	 KeyPair.setJob(jobDescription.toString());
             
             context.write(KeyPair, one);;
             KeyPair.setJob("*");
             context.write(KeyPair,one);
         }   
      }
	}

	public static class Reduce extends Reducer<KeyPairWritable, IntWritable, Text, Text> {
		private MapWritable Customer_Age = new MapWritable();
		IntWritable one = new IntWritable(1);
		
		@Override
		public void reduce(KeyPairWritable key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int age = key.getAge();
			String job = key.getJob();
			int count = 0;
			Text a = new Text(String.valueOf(age) + " " + job);
			Text b = new Text(String.valueOf(age));
			if (job.equals("*")) {			
				if (Customer_Age.get(b) == null) {
					Customer_Age.put(b, new IntWritable(Integer.parseInt("1")));
				} 
				else {
					int countInt = Integer.parseInt(Customer_Age.get(b).toString());
					countInt++;
					Customer_Age.put(b, new IntWritable(countInt));
				}
			} 
			else {
				for (IntWritable value: values) {
					System.out.println(value.toString());
					count += Integer.parseInt(value.toString());
				}
				
				if (Customer_Age.get(b) != null) {
					int total_Age = Integer.parseInt(Customer_Age.get(b).toString());	
					
					int total_Job = count;
					float percentage = (float)((float)total_Job / (float)total_Age) * 100;
					DecimalFormat df = new DecimalFormat("##.##");
					df.setRoundingMode(RoundingMode.DOWN);
					context.write(a, new Text(String.valueOf(df.format(percentage))));
				}
			}
		}
	}
	
	/*public static class Combine extends Combiner<KeyPairWritable, IntWritable, Text, Text> {
		private MapWritable Customer_Age = new MapWritable();
		IntWritable one = new IntWritable(1);
		
		@Override
		public void reduce(KeyPairWritable key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int age = key.getAge();
			String job = key.getJob();
			int count = 0;
			Text a = new Text(String.valueOf(age) + " " + job);
			Text b = new Text(String.valueOf(age));
			if (job.equals("*")) {			
				if (Customer_Age.get(b) == null) {
					Customer_Age.put(b, new IntWritable(Integer.parseInt("1")));
				} 
				else {
					int countInt = Integer.parseInt(Customer_Age.get(b).toString());
					countInt++;
					Customer_Age.put(b, new IntWritable(countInt));
				}
			} 
			else {
				for (IntWritable value: values) {
					System.out.println(value.toString());
					count += Integer.parseInt(value.toString());
				}
				
				if (Customer_Age.get(b) != null) {
					int total_Age = Integer.parseInt(Customer_Age.get(b).toString());	
					
					int total_Job = count;
					float percentage = (float)((float)total_Job / (float)total_Age) * 100;
					DecimalFormat df = new DecimalFormat("##.##");
					df.setRoundingMode(RoundingMode.DOWN);
					System.out.println(a.toString() + " total => " + total_Age + " byJob => " + total_Job + " percentage => " + df.format(percentage));
					context.write(a, new Text(String.valueOf(df.format(percentage))));
				}
			}
		}
	}*/
	
	
	
	public static class JobPartitioner extends Partitioner<KeyPairWritable, IntWritable> {
		@Override
		public int getPartition(KeyPairWritable key, IntWritable value, int numPartitions) { 
			return key.getJob().hashCode() % numPartitions;
		}
	}
	
	public static class KeyPairWritable implements WritableComparable < KeyPairWritable > {

		  /********* create two class attributes one for age and other for the job type********/
		  int age;
		  String job;


		  /**
		   * Empty constructor - required for serialization.
		   */
		  public KeyPairWritable() {

		  }

		  /**
		   * Constructor with two String objects provided as input.
		   */
		  public KeyPairWritable(int a, String j) {
		   age = a;
		   job = j;
		  }

		  /**
		   * Serializes the fields of this object to out.
		   */
		  public void write(DataOutput out) throws IOException {
		   out.writeInt(age);
		   out.writeUTF(job);

		  }

		  public void setAge(int age) {
			  this.age = age;
		  }
		  
		  public void setJob(String job) {
			  this.job = job;
		  }
		  
		  public int getAge() {
			  return this.age;
		  }
		  
		  public String getJob() {
			  return this.job;
		  }
		  
		  /**
		   * Deserializes the fields of this object from in.
		   */
		  public void readFields(DataInput in ) throws IOException {
		   age = in .readInt();
		   job = in .readUTF();
		  }

		  /**
		   * Compares this object to another KeyPairWritable object 
		   */
		  public int compareTo(KeyPairWritable other) {
		   /******* your code for comparing goes here******/
			  if (this.age < other.getAge()) {
				  return -1;
			  } else if (this.age > other.getAge()) {
				  return 1;
			  }
		      if (this.job.equals("*")){
		          return -1;
		      } else if(other.job.equals("*")){
		          return 1;
		      }
		      return this.job.compareTo(other.job);

		  }

		  /**
		   * A custom method that returns the two strings in the  StringPairWritable object inside parentheses and separated by
		   * a comma. For example: "(left,right)".
		   */
		  public String toString() {
		   return "(" + age + "," + job + ")";
		  }

		  /**
		   * The hashCode method generates a hash code for a StringPairWritable object. The equals and hashCode methods have been automatically
		   * generated by Eclipse by right-clicking on an empty line, selecting Source, and then selecting the Generate hashCode() and equals() option. 
		   */
		  @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + ((job == null) ? 0 : job.hashCode());;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj)
		    return true;
		   if (obj == null)
		    return false;
		   if (getClass() != obj.getClass())
		    return false;
		   KeyPairWritable other = (KeyPairWritable) obj;
		   if (job == null) {
		    if (other.job != null)
		     return false;
		   } else if (!job.equals(other.job))
		    return false;
		   if (age != other.age)
		    return false;
		   return true;
		  }


		 }

	

}
