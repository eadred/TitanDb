//import com.tinkerpop.gremlin.scala._
//import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory

import scala.collection.JavaConverters._
import org.apache.tinkerpop.gremlin.driver.{Client, Cluster}
import org.apache.commons.configuration.PropertiesConfiguration


/**
  * Created by eabi on 21/03/2017.
  */
object Main {
  def main(args:Array[String]) = {
    val config = new PropertiesConfiguration("gremlinclient.properties")

    val cluster = Cluster.open(config)

    val client = cluster.connect[Client]()

    val result = client.submit("100-90");

    System.out.println("Processing results...")
    result.stream().forEach(r => System.out.println(s"Result was ${r.getInt}"))
    System.out.println("Done!")

    client.close()
  }
}
