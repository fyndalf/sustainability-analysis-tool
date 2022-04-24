import cost.{
  ActivityIdentifier,
  ActivityProfile,
  ConcreteCostDriver,
  TraceProfile
}

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
