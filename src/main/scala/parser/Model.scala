package parser

import cost.{ActivityIdentifier, ProcessCost, ProcessCostDifference}

import java.awt.Color
import java.io.{File, PrintWriter}
import java.nio.file.Path
import scala.collection.immutable.HashMap
import scala.language.postfixOps
import scala.xml.*

object Model:

  // for each activity: find <bpmn:task> and note id
  // find bpmnd:BPMNShape with id and set color:background-color to whatever is needed

  /** Loads a model as XML from disk
    */
  def loadModelFromDisk(path: Path): Elem =
    XML.loadFile(path.toFile)

  /** Highlights process costs in a process model by setting
    * "color:background-color" attributes for the respective "bpmnd:BPMNShape"s.
    */
  def highlightCostInModel(cost: ProcessCost, modelPath: Path): Unit =

    // extract pure activity names from process cost
    val activityNames = cost.averageActivityCost.keys.map(_.id).toList
    // read model and extract mapping of task ids to activity identifiers
    val modelIDToActivityMap =
      extractModelActivityMapping(activityNames, modelPath)

    // read process model linewise
    val source = io.Source.fromFile(modelPath.toFile)
    val sourceLines = source.getLines()
    val processedModel = for
      in <- sourceLines
      out <-
        if in.contains("bpmndi:BPMNShape") && in.contains(
            "bpmnElement="
          ) && modelIDToActivityMap.keys.exists(in.contains)
        then
          // determine colour based on activity cost
          val activityID = modelIDToActivityMap.keys.find(in.contains).get
          val color = toHex(
            determineActivityColourForCost(
              modelIDToActivityMap(activityID),
              cost
            )
          )
          // append colour to last xml tag in line
          val colorString = s"color:background-color=\"$color\""
          s"${in.dropRight(1)} $colorString>"
        else in
    yield out

    saveNewProcessModel(modelPath, processedModel)
    source.close()

  def highlightCostDifferenceInModel(
      costDifference: ProcessCostDifference,
      modelPath: Path,
      isComparisonBasedOnAverage: Boolean
  ): Unit =

    // extract pure activity names from process cost
    val activityNames =
      costDifference.activityCostDifference.keys.map(_.id).toList
    // read model and extract mapping of task ids to activity identifiers
    val modelIDToActivityMap =
      extractModelActivityMapping(activityNames, modelPath)

    // read process model linewise
    val source = io.Source.fromFile(modelPath.toFile)
    val sourceLines = source.getLines()
    val processedModel = for
      in <- sourceLines
      out <-
        if in.contains("bpmndi:BPMNShape") && in.contains(
            "bpmnElement="
          ) && modelIDToActivityMap.keys.exists(in.contains)
        then
          // determine colour based on difference
          val activityID = modelIDToActivityMap.keys.find(in.contains).get
          val color = toHex(
            determineColourForCostDifference(
              modelIDToActivityMap(activityID),
              costDifference,
              isComparisonBasedOnAverage
            )
          )
          // append colour to last xml tag in line
          val colorString = s"color:background-color=\"$color\""
          s"${in.dropRight(1)} $colorString>"
        else in
    yield out

    saveNewProcessModel(modelPath, processedModel)
    source.close()

  /** Reads a process model and determines the assignment of "bpmn:task" ids to
    * activity identifiers
    */
  private def extractModelActivityMapping(
      activityNames: List[String],
      modelPath: Path
  ): Map[String, ActivityIdentifier] =
    val model = loadModelFromDisk(modelPath)

    // for activity of process cost: note task id
    var modelIDToActivityMap = HashMap[String, ActivityIdentifier]()

    model.child
      .filter(node => node.label == "process")
      .foreach(processNode =>
        processNode.child
          .filter(taskNode => taskNode.label == "task")
          .foreach(taskNode =>
            if taskNode
                .attribute("id")
                .isDefined && taskNode
                .attribute("name")
                .isDefined && activityNames
                .contains(taskNode.attribute("name").get.text)
            then
              // store task id and corresponding activity identifier
              modelIDToActivityMap = modelIDToActivityMap + (taskNode
                .attribute("id")
                .get
                .text -> ActivityIdentifier(
                taskNode.attribute("name").get.text
              ))
          )
      )

    modelIDToActivityMap
