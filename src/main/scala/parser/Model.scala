package parser

import cost.{ActivityIdentifier, ProcessCost, ProcessCostDifference}

import java.awt.Color
import java.io.{File, PrintWriter}
import java.nio.file.Path
import scala.collection.immutable.HashMap
import scala.language.postfixOps
import scala.xml.*

object Model:

  // determine colour difference based on: max difference between differences, or on percentual increase

  // for each activity: find <bpmn:task> and note id
  // find bpmnd:BPMNShape with id and set color:background-color to whatever is needed

  def loadModelFromDisk(path: Path): Elem =
    XML.loadFile(path.toFile)

  def highlightCostInModel(cost: ProcessCost, modelPath: Path): Unit =

    val activityNames = cost.averageActivityCost.keys.map(_.id).toList
    val modelIDToActivityMap =
      extractModelActivityMapping(activityNames, modelPath)

    val source = io.Source.fromFile(modelPath.toFile)
    val sourceLines = source.getLines()
    val processedModel = for
      in <- sourceLines
      out <-
        if in.contains("bpmndi:BPMNShape") && in.contains(
            "bpmnElement="
          ) && modelIDToActivityMap.keys.exists(in.contains)
        then
          val activityID = modelIDToActivityMap.keys.find(in.contains).get
          val color = toHex(
            determineActivityColourForCost(
              modelIDToActivityMap(activityID),
              cost
            )
          )
          val colorString = s"color:background-color=\"$color\""
          s"${in.dropRight(1)} $colorString>"
        else in
    yield out

    saveNewProcessModel(modelPath, processedModel)
    source.close()

  def highlightCostDifferenceInModel(
      costDifference: ProcessCostDifference,
      modelPath: Path
  ): Unit =

    val activityNames =
      costDifference.activityCostDifference.keys.map(_.id).toList
    val modelIDToActivityMap =
      extractModelActivityMapping(activityNames, modelPath)

    val source = io.Source.fromFile(modelPath.toFile)
    val sourceLines = source.getLines()
    val processedModel = for
      in <- sourceLines
      out <-
        if in.contains("bpmndi:BPMNShape") && in.contains(
            "bpmnElement="
          ) && modelIDToActivityMap.keys.exists(in.contains)
        then
          val activityID = modelIDToActivityMap.keys.find(in.contains).get
          val color = toHex(
            determineActivityColourForCostDifference(
              modelIDToActivityMap(activityID),
              costDifference
            )
          )
          val colorString = s"color:background-color=\"$color\""
          s"${in.dropRight(1)} $colorString>"
        else in
    yield out

    saveNewProcessModel(modelPath, processedModel)
    source.close()

  private def extractModelActivityMapping(
      activityNames: List[String],
      modelPath: Path
  ): Map[String, ActivityIdentifier] =
    val model = loadModelFromDisk(modelPath)

    // for activity of process cost: note task id
    var modelIDToActivityMap = HashMap[String, ActivityIdentifier]()

    model.child
      .filter(node => node.label == "process")
      .foreach(node =>
        node.child
          .filter(task => task.label == "task")
          .foreach(task =>
            if task
                .attribute("id")
                .isDefined && task.attribute("name").isDefined && activityNames
                .contains(task.attribute("name").get.text)
            then
              modelIDToActivityMap = modelIDToActivityMap + (task
                .attribute("id")
                .get
                .text -> ActivityIdentifier(
                task.attribute("name").get.text
              ))
          )
      )

    modelIDToActivityMap
