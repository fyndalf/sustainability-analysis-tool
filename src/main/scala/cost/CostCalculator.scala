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
      val activityCostBefore: Double =
        activityCostsBefore.getOrElse(activity, 0.0)
      val activityCostAfter: Double =
        activityCostsAfter.getOrElse(activity, 0.0)
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
