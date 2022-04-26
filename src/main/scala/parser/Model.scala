package parser

import cost.{ActivityIdentifier, ProcessCost}

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
    val model = loadModelFromDisk(modelPath)

    // for activity of process cost: note task id
    var activityToIdMap = HashMap[String, ActivityIdentifier]()
    val activityNames = cost.averageActivityCost.keys.map(_.id).toList

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
              activityToIdMap = activityToIdMap + (task
                .attribute("id")
                .get
                .text -> ActivityIdentifier(
                task.attribute("name").get.text
              ))
          )
      )

    val source = io.Source.fromFile(modelPath.toFile)
    val sourceLines = source.getLines()
    val processedModel = for
      in <- sourceLines
      out <-
        if in.contains("bpmndi:BPMNShape") && in.contains(
            "bpmnElement="
          ) && activityToIdMap.keys.exists(in.contains)
        then
          val activityID = activityToIdMap.keys.find(in.contains).get
          val color = toHex(
            determineActivityColour(activityToIdMap(activityID), cost)
          )
          val colorString = s"color:background-color=\"$color\""
          s"${in.dropRight(1)} $colorString>"
        else in
    yield out

    val pathToFile = modelPath.getParent.toString
    val filename =
      s"${modelPath.getFileName.toString.split(".bpmn")(0)}_highlighted.bpmn"

    val variable_name = new PrintWriter(s"$pathToFile/$filename")
    variable_name.write(processedModel.toArray)
    variable_name.close()
    source.close()
