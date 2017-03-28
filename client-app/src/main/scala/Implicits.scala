import gremlin.scala.{Edge, GremlinScala, Key, ScalaGraph, Vertex}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import shapeless.{HList, HNil}

/**
  * Created by eabi on 28/03/2017.
  */
object Implicits {

  implicit class ScalaGraphExtra(g:ScalaGraph) {
    def withSack[A](initialValue:A):GraphTraversalSource = {
      //Have to return the GraphTraversalSource rather than its graph since
      //the graph knows nothing about this traversal source and instead will create
      //a new one when it's traversal is requested (eg when asking for the vertices)
      g.graph.traversal.withSack(initialValue)
    }
  }

  implicit class GraphTraversalSourceExtra(gts:GraphTraversalSource) {
    def Vertices = GremlinScala[Vertex, HNil](gts.V())
    def Vertices(vertexIds:AnyRef*) = GremlinScala[Vertex, HNil](gts.V(vertexIds))
    def Edges = GremlinScala[Edge, HNil](gts.E())
    def Edges(edgeIds:AnyRef*) = GremlinScala[Edge, HNil](gts.E(edgeIds))
  }

  implicit class GremlinScalaExtra[End, Labels <: HList](gs:GremlinScala[End,Labels]) {
    def updateSack[V,U](fn: (V,U) => V):GremlinScala[End, Labels] = GremlinScala[End, Labels](gs.traversal.sack((x,y) => fn(x,y)))
  }

  implicit def toKey[A](keyName: String):Key[A] = Key(keyName)
}
