import cost.*

import java.awt.Color

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

    if difference > 100.0 && colour == Color.red then colour.darker()
    else if difference > 100 then colour.brighter()
    else if difference > 50.0 then colour
    else if colour == Color.green then colour.darker()
    else colour.brighter()

  private def percentageDifference(
      costA: Double,
      costB: Double
  ): Double =
    if costA == costB then return 0.0
    Math.abs(((costB - costA) / costA) * 100)

  private def toHex(colour: Color): String =
    s"#${Integer.toHexString(colour.getRGB).substring(2)}"
