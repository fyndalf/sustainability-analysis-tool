import cost.*

import java.awt.Color
import java.io.PrintWriter
import java.nio.file.Path

package object parser:
  // for parsing

  /** A cost variant, consisting of an id and a set of concrete cost drivers,
    * which contain concrete costs
    */
  case class CostVariant(id: String, costDrivers: List[ConcreteCostDriver])

  /** A cost variant config, consisting of both cost variants and fixed costs
    */
  case class CostVariantConfig(
      variants: List[CostVariant],
      fixedActivityCosts: Map[ActivityIdentifier, Double]
  )

  /** A parsed log, where all activity identifiers have been extracted and all
    * trace profiles have been calculated
    */
  case class ParsedLog(
      activities: Set[ActivityIdentifier],
      traceProfiles: List[TraceProfile]
  )

  /** For a given activity and a process cost, determines the colour based on
    * difference to the average cost of all activities. This indicates the
    * average impact an activity has.
    */
  def determineActivityColourForCost(
      activity: ActivityIdentifier,
      processCost: ProcessCost
  ): Color =
    val activityCost = processCost.averageActivityCost(activity)
    if activityCost == 0.0 then return Color.white
    val averageCost =
      processCost.averageActivityCost.values.sum / processCost.averageActivityCost.values.size

    determineColour(activityCost, averageCost)

  /** Determines the colour for a cost difference either with respect to the
    * average difference or with absolute terms
    */
  def determineColourForCostDifference(
      activity: ActivityIdentifier,
      processCostDifference: ProcessCostDifference,
      isComparisonBasedOnAverage: Boolean
  ): Color =
    if isComparisonBasedOnAverage then
      determineActivityColourForCostDifferenceFromAverageDifference(
        activity,
        processCostDifference
      )
    else
      determineActivityColourForCostDifferenceFromActualDifference(
        activity,
        processCostDifference
      )

  /** For a given activity and a process cost difference, determines the colour
    * based on difference to the average difference of all activities. This
    * indicates whether an activity has changed more or less than others.
    */
  def determineActivityColourForCostDifferenceFromAverageDifference(
      activity: ActivityIdentifier,
      processCostDifference: ProcessCostDifference
  ): Color =
    val costDifference = processCostDifference.activityCostDifference(activity)
    if costDifference == 0.0 then return Color.white
    // has activity changed more or less than average?

    // todo: what does average mean in this setting? Can it not just be trace cost / cost driver per trace?
    val averageCostDifference =
      processCostDifference.activityCostDifference.values
        .filter(_.isFinite)
        .sum / processCostDifference.activityCostDifference.values.count(
        _.isFinite
      )
    determineColour(costDifference, averageCostDifference)

  /** For a given activity and process cost difference, do the highlighting
    * based on absolute differences
    */
  def determineActivityColourForCostDifferenceFromActualDifference(
      activityIdentifier: ActivityIdentifier,
      processCostDifference: ProcessCostDifference
  ): Color =
    val costDifference =
      processCostDifference.activityCostDifference(activityIdentifier)
    if costDifference == 0.0 then return Color.white

    determineColour(costDifference, 0.0)

  /** Calculates the absolute percentage difference between two cost values
    */
  private def percentageDifference(
      costA: Double,
      costB: Double
  ): Double =
    if costA == costB then return 0.0
    Math.abs(((costB - costA) / costA) * 100)

  /** Determines the colour based on its cost or cost difference and relation to
    * the average cost or cost difference. Lower -> Green, Higher -> Red, with
    * intensity highlighting the degree of difference
    */
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

  /** Returns the hexadecimal representation of a colour
    */
  def toHex(colour: Color): String =
    s"#${Integer.toHexString(colour.getRGB).substring(2)}"

  /** Generates a new filename for the augmented model.
    */
  def newFilenameForModel(modelPath: Path): String =
    val pathToFile = modelPath.getParent.toString
    val filename =
      s"${modelPath.getFileName.toString.split(".bpmn")(0)}_highlighted.bpmn"
    s"$pathToFile/$filename"

  /** Saves the augmented process model at a certain path.
    */
  def saveNewProcessModel(
      modelPath: Path,
      processedModel: Iterator[Char]
  ): Unit =
    val variable_name = new PrintWriter(newFilenameForModel(modelPath))
    variable_name.write(processedModel.toArray)
    variable_name.close()
