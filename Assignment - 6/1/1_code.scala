package bigdata

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.Partitioner
import org.apache.spark.sql.functions.spark_partition_id
import scala.collection.immutable.ListMap
import scala.math
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.not

object WordCount {
  def main(args: Array[String]){
    val infile = args(0)
    val spark=SparkSession.builder().master("local[*]").appName("SS").getOrCreate()
    
import spark.implicits._
    val data = spark.read.format("csv").option("sep", ";").option("inferSchema", "true").option("header", "true").load(infile)
    data.show()
    
    // Total Number of employees
    data.groupBy("job").count().show()
	
    // Minimum Balance for each job
    data.groupBy("job").min("balance").show()
	
    // Maximum Balance for each job
    data.groupBy("job").max("balance").show()
	
    // Average Balance for each job
    data.groupBy("job").avg("balance").show()
	
    // Single vs Married - Education Comparison
    data.filter(($"marital" === "single") || ($"marital" === "married") && not($"education" === "unknown")).groupBy("marital").count().show()
  }
}