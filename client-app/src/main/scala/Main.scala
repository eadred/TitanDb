import org.apache.tinkerpop.gremlin.tinkergraph.structure.{TinkerEdge, TinkerGraph, TinkerProperty}
import org.apache.tinkerpop.gremlin.driver.Cluster
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph
import scala.collection.JavaConverters._
import Implicits._


/**
  * Created by eabi on 21/03/2017.
  */
object Main {

  import gremlin.scala._

  //Required in `match`
  import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__

  def main(args:Array[String]) = {
    tryOutSubgraph
    tryOutElementProperties
  }

  def tryOutSubgraph = {
    val g = TinkerGraph.open.asScala

    val proj1 = g.addVertex("Project", ("name", "Project A"))
    val proj2 = g.addVertex("Project", ("name", "Project B"))
    val proj3 = g.addVertex("Project", ("name", "Project C"))

    val per1 = g.addVertex("Person", ("name", "Alice"))
    val per2 = g.addVertex("Person", ("name", "Bob"))
    val per3 = g.addVertex("Person", ("name", "Charlie"))
    val per4 = g.addVertex("Person", ("name", "Daisy"))

    proj2.addEdge("DependsOn", proj3)

    per1.addEdge("WorkedOn", proj1)
    per2.addEdge("WorkedOn", proj2)
    per3.addEdge("WorkedOn", proj2)
    per4.addEdge("WorkedOn", proj3)

    // Find all people connected to Project B
    val pathsAndWeightsTraversal = g.withSack(1.0).Vertices.hasLabel("Person").as("person") //Start at all people
      .repeat(v =>
        v.bothE().hasLabel("WorkedOn","DependsOn") //Find all simple paths through WorkedOn and DependsOn relations...
        .updateSack[Double,TinkerEdge]((x,_) => x * 0.5) //...where for every edge we encounter attenuate the sack (people further away are less important) - potentially could use the edge type to determine the amount of attenuation
        .bothV().simplePath())
      .until(v => v.has("name"->"Project B")).as("project") //Only consider paths that end up at Project B
      .path.as("path") //Get hold of these paths
      .select("project").sack[Double]().as("sack") //Back up and get hold of the sack value for each path too
      .select("person", "path", "sack")

    //List of maps, with each map containing the person node, the path and the sack value
    val pathsAndWeights = pathsAndWeightsTraversal.traversal.toList

    //Find the sub-graph consisting of all people connected to Project B
    val pathsToProj2 = g.V.hasLabel("Person") //Start at all people
      .repeat(v => v.bothE().hasLabel("WorkedOn","DependsOn").bothV().simplePath()) //Find all simple paths through WorkedOn and DependsOn relations...
      .until(v => v.has("name"->"Project B")) //...but only those that end up at Project B
      .path()

    val proj2SubGraph = pathsToProj2.unfold() //Get all steps (vertices and edges) of all these paths...
      .filter((a:AnyRef) => a.isInstanceOf[TinkerEdge]).map((a:AnyRef) => a.asInstanceOf[TinkerEdge]) //...and filter to just the edges
      .subgraph(StepLabel("subgraph")).cap("subgraph") //Create sub-graph based on these edges
      .traversal.next().asInstanceOf[TinkerGraph].asScala
  }


  def tryOutElementProperties = {
    val g = TinkerGraph.open.asScala

    val p1 = g.addVertex("Person", ("name", "fred"), ("age", 97))
    val p2 = g.addVertex("Person", ("name", "daisy"), ("age", 82))
    val food1 = g.addVertex("Food", ("name", "Rice Pudding"))
    val food2 = g.addVertex("Food", ("name", "Boiled Onion"))
    val e = p1.addEdge("Knows", p2, "foo", 6.asInstanceOf[AnyRef], "bar", true.asInstanceOf[AnyRef])
    p1.addEdge("Eats", food1)
    p2.addEdge("Eats", food2)

    val fred = g.V.has("name"->"fred").toList()

    //Or alternatively, relying on the implicit conversion...
    //implicit def toTraversal(g:GremlinScala[_,_]) = g.traversal
    //val fred2 = g.V.has("name", "fred")

    val totAge = g.V.hasLabel("Person")
      .foldLeft(0)((tot,v) => tot + v.value[Int]("age"))
      .traversal
      .next()

    val matchResult = g.V.`match`(
      __.as("knower").out("Knows").as("knowee"),
      __.as("knowee").has("name", "daisy")
    )
      .select("knower", "knowee")
      .by("name")
      .toList

    val paths = g.V.outE.inV.path().by("name").by().toList  //How can we do by (label)?

    val nodes = g.V.repeat(v => v.both())
      .times(5)
      .values("name")
      .toList

    val eatsGraph = g.E.hasLabel("Eats")
      .subgraph(StepLabel("eatsGraph")).cap("eatsGraph").traversal
      .next().asInstanceOf[TinkerGraph] //Note - need the cast otherwise the type checker still thinks we are expecting an Edge
      .asScala

    def getKeyVals(el:Element) = {
      el
        .keys
        .asScala
        .map(k => {
          val prop:Property[TinkerProperty[AnyRef]] = el.property(k)
          (prop.key, prop.value)
        })
        .toList
    }

    val edgeKeyVals = getKeyVals(e)
    val v1KeyVals = getKeyVals(p1)
    val v2KeyVals = getKeyVals(p2)
  }


  def tryOutRemoteTraversal = {
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
