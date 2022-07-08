package cost

import cost.calculation.{averageActivityCost, averageTraceProfileCost}
import parser.ParsedLog

object CostCalculator:

  /** Calculate cost for a single parsed log
    */
  def calculateProcessCost(log: ParsedLog): ProcessCost =
    // for each activity, store average cost
    var averageActivityCosts = Map[ActivityIdentifier, Double]()
    // calculate average trace cost
    val averageTraceCost = averageTraceProfileCost(log.traceProfiles)

    // calculate average activity costs
    log.activities.foreach(activity =>
      averageActivityCosts =
        averageActivityCosts + (activity -> averageActivityCost(
          activity,
          log.traceProfiles
        ))
    )

    ProcessCost(averageTraceCost, averageActivityCosts)

  /** Calculate differences between two process costs, either in absolute or
    * relative terms
    */
  def calculateCostDifferences(
      costBefore: ProcessCost,
      costAfter: ProcessCost,
      isRelativeCalculation: Boolean
  ): ProcessCostDifference =

    // calculate trace cost difference
    val traceCostBefore = costBefore.averageTraceCost
    val traceCostAfter = costAfter.averageTraceCost
    val traceCostDifference = calculateCostDifference(
      traceCostBefore,
      traceCostAfter,
      isRelativeCalculation
    )

    // todo: mapping of different activities before/after renaming should be taken into account here!
    val activityCostsBefore = costBefore.averageActivityCost
    val activityCostsAfter = costAfter.averageActivityCost

    var activityCostDifference = Map[ActivityIdentifier, Double]()

    val combinedActivities =
      activityCostsBefore.keys.toList ++ activityCostsAfter.keys.toList

    // for all activities that exist in either one or the other log, calculate difference
    // and use 0.0 as cost before or after if an activity is not present in one of them
    combinedActivities.toSet.foreach(activity =>
      val activityCostBefore: Double =
        activityCostsBefore.getOrElse(activity, 0.0);
      val activityCostAfter: Double =
        activityCostsAfter.getOrElse(activity, 0.0);
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
