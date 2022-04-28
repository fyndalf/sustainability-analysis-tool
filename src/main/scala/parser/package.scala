import cost.*

import java.awt.Color
import java.io.PrintWriter
import java.nio.file.Path

package object parser:
  // for parsing

  case class CostVariant(id: String, costDrivers: List[ConcreteCostDriver])

  case class CostVariantConfig(
      variants: List[CostVariant],
      fixedActivityCosts: Map[ActivityIdentifier, Double]
  )

  case class ParsedLog(
      activities: Set[ActivityIdentifier],
      traceProfiles: List[TraceProfile]
  )

  def parseActivityForCostVariant(
      variant: CostVariant,
      activityIdentifier: String,
      loggedCostDrivers: List[String],
      fixedActivityCosts: Map[ActivityIdentifier, Double]
  ): ActivityProfile =
    val id = ActivityIdentifier(activityIdentifier)
    val concreteCostDrivers =
      variant.costDrivers.filter(d => loggedCostDrivers.contains(d.name))
    val fixedCost =
      if fixedActivityCosts.contains(id) then fixedActivityCosts(id) else 0
    ActivityProfile(id, concreteCostDrivers, fixedCost)

  def determineActivityColourForCost(
      activity: ActivityIdentifier,
      processCost: ProcessCost
  ): Color =
    val activityCost = processCost.averageActivityCost(activity)
    if activityCost == 0.0 then return Color.white
    val averageCost =
      processCost.averageActivityCost.values.sum / processCost.averageActivityCost.values.size

    determineColour(activityCost, averageCost)

  def determineActivityColourForCostDifference(
      activity: ActivityIdentifier,
      processCostDifference: ProcessCostDifference
  ): Color =
    val costDifference = processCostDifference.activityCostDifference(activity)
    if costDifference == 0.0 then return Color.white

    val averageCostDifference =
      processCostDifference.activityCostDifference.values
        .filter(_.isFinite)
        .sum / processCostDifference.activityCostDifference.values.count(
        _.isFinite
      )

    determineColour(costDifference, averageCostDifference)

  private def percentageDifference(
      costA: Double,
      costB: Double
  ): Double =
    if costA == costB then return 0.0
    Math.abs(((costB - costA) / costA) * 100)

  private def determineColour(thisCost: Double, averageCost: Double): Color =
    val colour =
      if thisCost < averageCost then Color.green else Color.red
    val differenceToAverage =
      percentageDifference(thisCost, averageCost)

    if differenceToAverage > 100.0 && colour == Color.red then colour.darker()
    else if differenceToAverage > 100 then colour.brighter()
    else if differenceToAverage > 50.0 then colour
    else if colour == Color.green then colour.darker()
    else colour.brighter()

  def toHex(colour: Color): String =
    s"#${Integer.toHexString(colour.getRGB).substring(2)}"

  def newFilenameForModel(modelPath: Path): String =
    val pathToFile = modelPath.getParent.toString
    val filename =
      s"${modelPath.getFileName.toString.split(".bpmn")(0)}_highlighted.bpmn"
    s"$pathToFile/$filename"

  def saveNewProcessModel(
      modelPath: Path,
      processedModel: Iterator[Char]
  ): Unit =
    val variable_name = new PrintWriter(newFilenameForModel(modelPath))
    variable_name.write(processedModel.toArray)
    variable_name.close()
