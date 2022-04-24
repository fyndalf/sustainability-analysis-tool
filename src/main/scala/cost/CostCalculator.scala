package cost

import parser.ParsedLog

import scala.language.postfixOps

object CostCalculator:

  def calculateProcessCost(log: ParsedLog): ProcessCost =

    var averageActivityCosts = Map[ActivityIdentifier, Double]()

    val averageTraceCost = averageTraceProfileCost(log.traceProfiles)

    log.activities.foreach(activity =>
      averageActivityCosts =
        averageActivityCosts + (activity -> averageActivityCost(
          activity,
          log.traceProfiles
        ))
    )

    ProcessCost(averageTraceCost, averageActivityCosts)

  def calculateCostDifferences(
      costBefore: ProcessCost,
      costAfter: ProcessCost,
      isRelativeCalculation: Boolean = true
  ): ProcessCostDifference =

    val traceCostBefore = costBefore.averageTraceCost
    val traceCostAfter = costAfter.averageTraceCost
    val traceCostDifference = calculateCostDifference(
      traceCostBefore,
      traceCostAfter,
      isRelativeCalculation
    )

    val activityCostsBefore = costBefore.averageActivityCost
    val activityCostsAfter = costAfter.averageActivityCost

    var activityCostDifference = Map[ActivityIdentifier, Double]()

    val combinedActivities =
      activityCostsBefore.keys.toList ++ activityCostsAfter.keys.toList

    combinedActivities.toSet.foreach(activity =>
      val activityCostBefore = activityCostsBefore.getOrElse(activity, 0.0)
      val activityCostAfter = activityCostsAfter.getOrElse(activity, 0.0)
      activityCostDifference =
        activityCostDifference + (activity -> calculateCostDifference(
          activityCostBefore,
          activityCostAfter,
          isRelativeCalculation
        ))
    )

    ProcessCostDifference(
      traceCostDifference,
      activityCostDifference,
      isRelativeCalculation
    )

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
