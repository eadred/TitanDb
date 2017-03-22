import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.{TinkerFactory, TinkerGraph}

import scala.collection.JavaConverters._
import org.apache.tinkerpop.gremlin.driver.{Client, Cluster}
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph


/**
  * Created by eabi on 21/03/2017.
  */
object Main {
  def main(args:Array[String]) = {
    val config = new PropertiesConfiguration("gremlinclient.properties")

    val cluster = Cluster.open(config)

    //Set up a remote source
    val traversalSource = EmptyGraph.instance().traversal().withRemote(DriverRemoteConnection.using(cluster, "g"))

    //Alternatively set up the traversal source without explicitly creating a cluster using a properties file,
    // which in turn references a yaml file containing the same config as the config the cluster uses
    //val remoteConfig = new PropertiesConfiguration("gremlinremote.properties")
    //val traversalSource = EmptyGraph.instance().traversal().withRemote(remoteConfig)

    val traversal = gremlin.scala.wrap(traversalSource.V()).value[String]("name")

    //The problem is this doesn't work with TitanDB in the backend since this is still using
    //Tinkerpop 3.0.x which doesn't support remote graphs yet
    //See https://groups.google.com/forum/#!topic/gremlin-users/N-56-tQRUhQ

    val result = traversal.toList()

    System.out.println("Processing results...")
    result.foreach(r => System.out.println(s"Result was '$r'"))
    System.out.println("Done!")

    traversalSource.close()
    cluster.close()
  }
}
