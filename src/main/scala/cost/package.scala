package object cost {

  case class ActivityIdentifier(id: String)

  case class AbstractCostDriver(name: String)

  case class ConcreteCostDriver(name: String, cost: Double)

  case class ActivityProfile(activityIdentifier: ActivityIdentifier, concreteCostDriver: List[ConcreteCostDriver], fixedCost: Double)

  case class TraceProfile(activityProfiles: List[ActivityProfile])

  // for score calculation

  def activityProfileCost(activityProfile: ActivityProfile): Double = {
    activityProfile.concreteCostDriver.map(_.cost).sum + activityProfile.fixedCost
  }

  def traceProfileCost(traceProfile: TraceProfile): Double = {
    traceProfile.activityProfiles.map(activityProfileCost).sum
  }

  def averageTraceProfileCost(traceProfiles: List[TraceProfile]): Double = {
    traceProfiles.map(traceProfileCost).sum / traceProfiles.size
  }

  def averageActivityCost(activityIdentifier: ActivityIdentifier, traceProfiles: List[TraceProfile]): Double = {
    val totalCost = traceProfiles
      .map(
        _.activityProfiles
          .filter(_.activityIdentifier == activityIdentifier)
          .map(activityProfileCost).sum).sum
    val totalOccurrence = traceProfiles.map(occurrence_count(activityIdentifier, _)).sum
    totalCost / totalOccurrence
  }

  private def occurrence_count(activityIdentifier: ActivityIdentifier, traceProfile: TraceProfile): Int = {
    traceProfile.activityProfiles.count(_.activityIdentifier == activityIdentifier)
  }

}