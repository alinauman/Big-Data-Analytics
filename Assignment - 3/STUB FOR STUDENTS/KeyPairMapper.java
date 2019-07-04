package bigdata.keypair;


import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class KeyPairMapper extends Mapper<LongWritable, Text, KeyPairWritable, IntWritable> {

	IntWritable one = new IntWritable(1);
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		
		/*
		 * Split the line. Create a new KeyPairWritable consisting  age and job type. Emit the pair as the key, and
		 * '1' as the value (for later summing).
		 */
		
/* Also emit the (age, *) pair to help in calculating the total customers of particular age. 
			
		
	}
}