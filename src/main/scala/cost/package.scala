import cost.{ProcessCost, ProcessCostDifference}

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

  /** Contains cost calculation methods for the different types of profile costs
    */
  object calculation:
    def activityProfileCost(activityProfile: ActivityProfile): Double =
      // sum of all concrete cost drivers and fixed costs
      activityProfile.concreteCostDriver
        .map(_.cost)
        .sum + activityProfile.fixedCost

    def traceProfileCost(traceProfile: TraceProfile): Double =
      // sum of all activity profile costs
      traceProfile.activityProfiles.map(activityProfileCost).sum

    def averageTraceProfileCost(traceProfiles: List[TraceProfile]): Double =
      // the average of all trace profile costs
      traceProfiles.map(traceProfileCost).sum / traceProfiles.size

    def averageActivityCost(
        activityIdentifier: ActivityIdentifier,
        traceProfiles: List[TraceProfile]
    ): Double =
      // the average cost of an activity across all trace profiles and their activity profiles

      // total cost of activity across all trace profiles
      val totalCost = traceProfiles
        .map(
          _.activityProfiles
            .filter(_.activityIdentifier == activityIdentifier)
            .map(activityProfileCost)
            .sum
        )
        .sum
      // number of occurrence of activity in all trace profiles
      val totalOccurrence =
        traceProfiles.map(occurrence_count(activityIdentifier, _)).sum
      totalCost / totalOccurrence

    def occurrence_count(
        activityIdentifier: ActivityIdentifier,
        traceProfile: TraceProfile
    ): Int =
      // number of occurrences of activity in trace profile
      traceProfile.activityProfiles.count(
        _.activityIdentifier == activityIdentifier
      )

  /** Calculate cost difference between two cost values, either in absolute or
    * relative terms
    */
  def calculateCostDifference(
      costBefore: Double,
      costAfter: Double,
      isRelativeCalculation: Boolean
  ): Double =
    if isRelativeCalculation then
      calculatePercentageIncrease(costBefore, costAfter)
    else costAfter - costBefore

  /** Calculate change of two cost values in terms of percentages
    */
  def calculatePercentageIncrease(
      costBefore: Double,
      costAfter: Double
  ): Double =
    // todo: how to handle NaN or Infinity scenarios here?
    if costBefore == costAfter then return 0.0
    ((costAfter - costBefore) / costBefore) * 100

  object printer:
    def printProcessCost(cost: ProcessCost): Unit =
      println("\n##### Process Cost #####\n")
      println(s"Average Trace Cost: ${cost.averageTraceCost}")
      println("Activity Costs:")
      cost.averageActivityCost
        .foreach(activityCost =>
          println(s"    ${activityCost._1.id} - ${activityCost._2}")
        )
      println("\n")

    def printProcessCostDifference(
        costDifference: ProcessCostDifference
    ): Unit =
      val mode =
        if costDifference.isDifferenceRelative then "Relative" else "Absolute"
      println(s"\n##### $mode Process Cost Difference #####\n")
      println(s"Trace Cost Difference: ${costDifference.traceCostDifference}")
      println("Activity Cost Differences:")
      costDifference.activityCostDifference
        .foreach(activityCost =>
          println(s"    ${activityCost._1.id} - ${activityCost._2}")
        )
      println("\n")
