package parser

import cost.{ActivityIdentifier, ProcessCost}

import java.awt.Color
import scala.xml.{Attribute, Elem, Node, Null, UnprefixedAttribute, XML}
import java.nio.file.Path
import scala.collection.immutable.HashMap
import scala.language.postfixOps
import math.Integral.Implicits.infixIntegralOps

object Model:

  // determine colour difference based on: max difference between differences, or on percentual increase

  // for each activity: find <bpmn:task> and note id
  // find bpmnd:BPMNShape with id and set color:background-color="#ffffff" color:border-color="#FF6600"

  def loadModelFromDisk(path: Path): Elem =
    XML.loadFile(path.toFile)

  // todo: make this return the file?
  def highlightCostInModel(cost: ProcessCost, modelPath: Path): Unit =
    val model = loadModelFromDisk(modelPath)

    // for activity of process cost: note task id
    var activityToIdMap = HashMap[String, ActivityIdentifier]()
    val activityNames = cost.averageActivityCost.keys.map(_.id).toList

    println(activityNames)

    model.child
      .filter(node => node.label == "process")
      .foreach(node =>
        node.child
          .filter(task => task.label == "task")
          .foreach(task =>
            println(task.attribute("id"))
            if task
                .attribute("id")
                .isDefined && task.attribute("name").isDefined && activityNames
                .contains(task.attribute("name").get.text)
            then
              activityToIdMap = activityToIdMap + (task
                .attribute("id")
                .get
                .text -> ActivityIdentifier(
                task.attribute("name").get.text
              ))
          )
      )

    println(activityToIdMap)

    val activityIds = activityToIdMap.keys.toList

    var replacementMap = HashMap[Node, Elem]()

    model.child
      .filter(node => node.label == "BPMNDiagram")
      .foreach(diagram =>
        diagram.child
          .filter(node => node.label == "BPMNPlane")
          .foreach(plane =>
            plane.child
              .filter(shape => shape.label == "BPMNShape")
              .foreach(shape =>
                var newShape: Elem = shape.asInstanceOf[Elem]
                if shape.attribute("bpmnElement").isDefined && activityIds
                    .contains(shape.attribute("bpmnElement").get.text)
                then
                  val activityName =
                    activityToIdMap.get(shape.attribute("bpmnElement").get.text)
                  val activityCost =
                    cost.averageActivityCost.get(activityName.get)
                  if activityCost.isDefined
                  then
                    newShape = newShape.%(
                      UnprefixedAttribute(
                        "color:background-color",
                        value = toHex(
                          determineActivityColour(activityName.get, cost)
                        ),
                        Null
                      )
                    )
                    replacementMap = replacementMap + (shape -> newShape)
              )
          )
      )

    // todo: set new nodes in BPMN
    ???

    println(model)

  def updateVersion(node: Node): Node =
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode <- seq) yield updateVersion(subNode)

    node match
      case <bpmndi:BPMNDiagram>{ch @ _}</bpmndi:BPMNDiagram> =>
        <bpmndi:BPMNDiagram>{updateElements(ch)}</bpmndi:BPMNDiagram>
      case <bpmndi:BPMNPlane>{ch @ _}</bpmndi:BPMNPlane> =>
        <bpmndi:BPMNPlane>{updateElements(ch)}</bpmndi:BPMNPlane>
      case <bpmndi:BPMNShape>{contents}</bpmndi:BPMNShape> =>
        <version>2</version>
      case other @ _ => other

  private def determineActivityColour(
      activity: ActivityIdentifier,
      processCost: ProcessCost
  ): Color =

    val activityCost = processCost.averageActivityCost(activity)

    if activityCost == 0.0 then return Color.white

    val averageCost =
      processCost.averageActivityCost.values.sum / processCost.averageActivityCost.values.size

    val colour = if activityCost < averageCost then Color.green else Color.red
    val difference = percentageDifference(activityCost, averageCost)

    if difference > 100.0 then colour.darker()
    else if difference > 50.0 then colour
    else colour.brighter()

  private def percentageDifference(
      costA: Double,
      costB: Double
  ): Double =
    if costA == costB then return 0.0
    Math.abs(((costB - costA) / costA) * 100)

  private def toHex(colour: Color): String =
    "#" + Integer.toHexString(colour.getRGB).substring(2)
