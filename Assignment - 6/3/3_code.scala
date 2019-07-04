package bigdata

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.Partitioner
import org.apache.spark.sql.functions.spark_partition_id
import scala.collection.immutable.ListMap
import scala.math
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.not
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors


object WordCount {
  def main(args: Array[String]){
    val infile = args(0)
    val outfile = args(1)
    val spark=SparkSession.builder().master("local[*]").appName("SS").getOrCreate()

    
import spark.implicits._
    val data = spark.read.format("csv")
                    .option("sep", ";")
                    .option("inferSchema", "true")
                    .option("header", "true")
                    .load(infile)
                    
    val child_data = data.rdd.map(x => (x(0).toString().toDouble,
                                        x(5).toString().toDouble,
                                        x(9).toString().toDouble,
                                        x(11).toString().toDouble,
                                        x(12).toString().toDouble,
                                        x(13).toString().toDouble,
                                        x(14).toString().toDouble)).cache()
    val parsedData = child_data.map(x => Vectors.dense(x._1, x._2,x._3, x._4,x._5, x._6,x._7))
    parsedData.saveAsTextFile(outfile)
	
    // Cluster the data into classes using KMeans
    val numClusters = 6
    val numIterations = 20
    val clusters = KMeans.train(parsedData, numClusters, numIterations)
    
    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(parsedData)
    println("/nWithin Set Sum of Squared Errors = " + WSSSE)
    
  }
}
