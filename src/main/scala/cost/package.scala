package object cost:

  case class ActivityIdentifier(id: String)

  case class AbstractCostDriver(name: String)

  case class ConcreteCostDriver(name: String, cost: Double)

  case class ActivityProfile(
      activityIdentifier: ActivityIdentifier,
      concreteCostDriver: List[ConcreteCostDriver],
      fixedCost: Double
  )

  case class TraceProfile(activityProfiles: List[ActivityProfile])

  case class ProcessCost(
      averageTraceCost: Double,
      averageActivityCost: Map[ActivityIdentifier, Double]
  )

  case class ProcessCostDifference(
      traceCostDifference: Double,
      activityCostDifference: Map[ActivityIdentifier, Double],
      isDifferenceRelative: Boolean
  )

  // for score calculation

  def activityProfileCost(activityProfile: ActivityProfile): Double =
    activityProfile.concreteCostDriver
      .map(_.cost)
      .sum + activityProfile.fixedCost

  def traceProfileCost(traceProfile: TraceProfile): Double =
    traceProfile.activityProfiles.map(activityProfileCost).sum

  def averageTraceProfileCost(traceProfiles: List[TraceProfile]): Double =
    traceProfiles.map(traceProfileCost).sum / traceProfiles.size

  def averageActivityCost(
      activityIdentifier: ActivityIdentifier,
      traceProfiles: List[TraceProfile]
  ): Double =
    val totalCost = traceProfiles
      .map(
        _.activityProfiles
          .filter(_.activityIdentifier == activityIdentifier)
          .map(activityProfileCost)
          .sum
      )
      .sum
    val totalOccurrence =
      traceProfiles.map(occurrence_count(activityIdentifier, _)).sum
    totalCost / totalOccurrence

  private def occurrence_count(
      activityIdentifier: ActivityIdentifier,
      traceProfile: TraceProfile
  ): Int =
    traceProfile.activityProfiles.count(
      _.activityIdentifier == activityIdentifier
    )

  // pretty print

  def printProcessCost(cost: ProcessCost): Unit =
    println("##### Process Cost #####\n")
    println(s"Average Trace Cost: ${cost.averageTraceCost}")
    println("Activity Costs:")
    cost.averageActivityCost
      .foreach(a => println(s"    ${a._1.id} - ${a._2}"))

  def printProcessCostDifference(costDifference: ProcessCostDifference): Unit =
    val mode =
      if costDifference.isDifferenceRelative then "Relative" else "Absolute"
    println(s"##### $mode Process Cost Difference #####\n")
    println(s"Trace Cost Difference: ${costDifference.traceCostDifference}")
    println("Activity Cost Differences:")
    costDifference.activityCostDifference
      .foreach(a => println(s"    ${a._1.id} - ${a._2}"))

  private def calculateCostDifference(
      costBefore: Double,
      costAfter: Double,
      isRelativeCalculation: Boolean
  ): Double =
    if isRelativeCalculation then
      calculatePercentageIncrease(costBefore, costAfter)
    else costAfter - costBefore

  private def calculatePercentageIncrease(
      costBefore: Double,
      costAfter: Double
  ): Double =
    // todo: how to handle NaN or Infinity scenarios here?
    if costBefore == costAfter then return 0.0
    ((costAfter - costBefore) / costBefore) * 100
