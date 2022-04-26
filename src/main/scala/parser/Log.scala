package parser

import cost.{
  ActivityIdentifier,
  ActivityProfile,
  ConcreteCostDriver,
  TraceProfile
}
import parser.VariantConfig

import java.nio.file.Path
import scala.language.postfixOps
import scala.xml.XML

object Log:

  def parseLog(path: Path, variantConfig: CostVariantConfig): ParsedLog =
    val logXML = XML.loadFile(path.toFile)

    var traceProfiles = Seq[TraceProfile]()
    var activities = Set[ActivityIdentifier]()

    logXML.child.foreach(traceNode =>
      if traceNode.label == "trace" then

        var activityProfiles = Seq[ActivityProfile]()
        val costVariantIdentifier = traceNode.child
          .find(n =>
            n.attribute("key")
              .isDefined && n.attribute("key").get.text == "cost:variant"
          )
          .get
          .attribute("value")
          .get
          .text
        val currentCostVariant: CostVariant =
          variantConfig.variants.find(_.id == costVariantIdentifier).get
        traceNode.child.foreach(eventNode =>
          if eventNode.label == "event" then
            val currentActivityIdentifier = eventNode.child
              .find(eventAttributeNode =>
                eventAttributeNode
                  .attribute("key")
                  .isDefined && eventAttributeNode
                  .attribute("key")
                  .get
                  .text == "concept:name"
              )
              .get
              .attribute("value")
              .get
              .text
            val currentActivity: ActivityIdentifier =
              ActivityIdentifier(currentActivityIdentifier)
            activities = activities + currentActivity
            val fixedCost =
              variantConfig.fixedActivityCosts.getOrElse(currentActivity, 0.0)

            var concreteCostDrivers = Seq[ConcreteCostDriver]()
            eventNode.child.foreach(eventAttribute =>
              if eventAttribute.attribute("key").isDefined && eventAttribute
                  .attribute("key")
                  .get
                  .text == "cost:driver"
              then
                val costDriverIdentifier: String =
                  eventAttribute.attribute("value").get.text
                concreteCostDrivers =
                  concreteCostDrivers :+ currentCostVariant.costDrivers
                    .find(_.name == costDriverIdentifier)
                    .get
            )
            activityProfiles = activityProfiles :+ ActivityProfile(
              currentActivity,
              concreteCostDrivers.toList,
              fixedCost
            )
        )
        traceProfiles = traceProfiles :+ TraceProfile(activityProfiles.toList)
    )
    ParsedLog(activities, traceProfiles.toList)
