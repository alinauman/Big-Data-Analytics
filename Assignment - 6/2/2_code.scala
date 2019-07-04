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
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoderEstimator, StringIndexer}
import breeze.linalg.{squaredDistance, DenseVector, Vector}


object WordCount {
  
  def parseVector(line: String): Vector[Double] = {
    DenseVector(line.split(',').map(_.toDouble))
  }

  def closestPoint(p: Vector[Double], centers: Array[Vector[Double]]): Int = {
    var bestIndex = 0
    var closest = Double.PositiveInfinity

    for (i <- 0 until centers.length) {
      val tempDist = squaredDistance(p, centers(i))
      if (tempDist < closest) {
        closest = tempDist
        bestIndex = i
      }
    }

    bestIndex
  }
  
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
                    //.select("age", "job", "marital", "education","balance")
    data.show()
    val features = data.columns.filterNot(_.contains("age"))
    val encodedFeatures = features.flatMap { name =>
      
      val indexer = new StringIndexer().setInputCol(name).setOutputCol(name + "_Index")
      
      Array(indexer)
    }.toArray
    
    val pipeline = new Pipeline().setStages(encodedFeatures)
    val indexer_model = pipeline.fit(data)
    val data_transformed = indexer_model.transform(data)
    data_transformed.rdd.saveAsTextFile(outfile)
    val child_data = data_transformed.rdd.map(x => (x(0).toString().toDouble,
                                        x(5).toString().toDouble,
                                        x(17).toString().toDouble,
                                        x(18).toString().toDouble,
                                        x(19).toString().toDouble)).cache()
    child_data.saveAsTextFile(outfile)    
    child_data.toDF().show()
    //val parsedData = child_data.map(x => Vectors.dense(x._1, x._2,x._3, x._4,x._5))
   val child_data_1 = child_data.map(x => parseVector _).cache()                                
    val K = args(1).toInt
    val convergeDist = args(2).toDouble
    
    val kPoints = child_data_1.takeSample(withReplacement = false, K)
    var tempDist = 1.0

    while(tempDist > convergeDist) {
      val closest = data.map (p => (closestPoint(p, kPoints), (p, 1)))

      val pointStats = closest.reduceByKey{case ((p1, c1), (p2, c2)) => (p1 + p2, c1 + c2)}

      val newPoints = pointStats.map {pair =>
        (pair._1, pair._2._1 * (1.0 / pair._2._2))}.collectAsMap()

      tempDist = 0.0
      for (i <- 0 until K) {
        tempDist += squaredDistance(kPoints(i), newPoints(i))
      }

      for (newP <- newPoints) {
        kPoints(newP._1) = newP._2
      }
      println(s"Finished iteration (delta = $tempDist)")
    }
    
    
    println("Final centers:")
    kPoints.foreach(println)
    spark.stop()
 }
}
