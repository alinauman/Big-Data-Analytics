package bigdata

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.Partitioner
import org.apache.spark.sql.functions.spark_partition_id
import scala.collection.immutable.ListMap
import scala.math

// Custom Partitioner to partition campuse wise
class CustomPartitioner(numberOfPartitioner: Int) extends Partitioner {
  override def numPartitions: Int = numberOfPartitioner
   def getPartition(key: Any): Int = {
    //if(key.toString().apply(2)){
    if(key.toString().charAt(2) == 'F'){  
    0
    }else if(key.toString().charAt(2) == 'L'){
      1
    }else if(key.toString().charAt(2) == 'K'){
      2
    }else if(key.toString().charAt(2) == 'I'){
      3
    }else if(key.toString().charAt(2) == 'P'){
      4
    }
    else{
      5
    }
  }
// Java equals method to let Spark compare our Partitioner objects
  override def equals(other: Any): Boolean = other match {
    case partitioner: CustomPartitioner =>
    partitioner.numPartitions == numPartitions
    case _ =>
    false
  }
}


object WordCount {
  
  def printList(args: List[_]): Unit = {    
  args.foreach(println)
  }
  
  
  def main(args: Array[String]){

    if (args.length !=2 ) throw new Exception( "Enter the input and output file name ")
    val infile = args(0)
    val outfile = args(1)
    val conf = new SparkConf().setAppName("WordCount").setMaster("local")
    val sc = new SparkContext(conf)
    // Load the input file and filter the data
    val Data = sc.textFile(infile)
    val words = Data. flatMap(line => (line.split(";")))    
    val words2 = words.flatMap(line2 => line2.split(","))
    // Make sure that the range of the year 1995 - 2018
    val finalMap = words2.filter(x=> (x.length() >1 && x.substring(0,2).matches("[0-9][0-9]") && (Integer.parseInt(x.substring(0,2)) > 95 || Integer.parseInt(x.substring(0,2)) <18)))
    val counts = finalMap.map(word => (word, 1))  
    // Partition the data and persist
    val range = counts.partitionBy(new CustomPartitioner(5)).persist() 
    // Total number of students per campus
    val campus = finalMap.flatMap(x => x.substring(2,3))      
    val campusCount =  campus.map(word => (word, 1)).reduceByKey(_+_) 
    printList(campusCount.collect().toList)
    // Sorted i. Year wise, ii. lexicographic ordering, iii. last part following int ordering
    val strMap = words.map(word => (word.split(",").apply(0), (word.split(",").apply(1),word.split(",").apply(2))))  
    val sortedMap = strMap.sortByKey(true, 5)    
    printList(sortedMap.collect().toList)
    // Removing  the duplicates
    val duplicate = strMap.distinct(5)
    printList(duplicate.collect().toList)
    // Number of failure per campus
    val onlyFail = strMap.filter(x=> x._2._2.toString().equals("F"))
    val failCount = onlyFail.map(x => ((x._1.charAt(2), x._2._1),1)).reduceByKey(_+_) 
    printList(failCount.collect().toList)
    
    // GPA Calculation
    val rr = words.map(word => (word.split(",").apply(0), (if(word.split(",").apply(2).equals('A')) 4 else if(word.split(",").apply(2) == 'B') 3 else if(word.split(",").apply(2) == 'C') 2 else if(word.split(",").apply(2) == 'D') 1))     )  
    // Changing grade to points (Grade A GPA=4, Grade B GPA=3, Grade C GPA 2, Grade D GPA 1 and Grade F GPA=0)
    val kkj = strMap.map(x=> (x._1,(if(x._2._2.toString().equalsIgnoreCase("A")) 4 else if (x._2._2.toString().equalsIgnoreCase("B")) 3 else if (x._2._2.toString().equalsIgnoreCase("C")) 2 else if (x._2._2.toString().equalsIgnoreCase("D")) 1 else if (x._2._2.toString().equalsIgnoreCase("F")) 0)))
    val group = kkj.groupByKey(5)
    val count =  group.map(x => (x._1,x._2.count(x => true)))
    val rrsa =  group.map(x => x._2.head)
    
    range.saveAsTextFile(outfile)
    sc.stop
  }
}