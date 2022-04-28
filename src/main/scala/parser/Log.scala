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

  /**
   * Parses a log file to extract its activity identifiers and trace profiles
   */
  def parseLog(path: Path, variantConfig: CostVariantConfig): ParsedLog =
    val logXML = XML.loadFile(path.toFile)

    var traceProfiles = Seq[TraceProfile]()
    var activities = Set[ActivityIdentifier]()

    // for each trace tag in xml
    logXML.child.foreach(traceNode =>
      if traceNode.label == "trace" then

        // create activity profiles
        var activityProfilesOfTrace = Seq[ActivityProfile]()

        // get cost variant attribute
        val costVariantIdentifier = traceNode.child
          .find(traceAttribute =>
            traceAttribute.attribute("key")
              .isDefined && traceAttribute.attribute("key").get.text == "cost:variant"
          )
          .get
          .attribute("value")
          .get
          .text

        // get cost variant from attribute
        val costVariantOfTrace: CostVariant =
          variantConfig.variants.find(_.id == costVariantIdentifier).get;

        // for each event node, extract activity identifier and concrete cost drivers
        traceNode.child.foreach(eventNode =>
          if eventNode.label == "event" then

            // get activity identifier
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

            // store activity identifier for trace
            val currentActivity: ActivityIdentifier =
              ActivityIdentifier(currentActivityIdentifier);
            activities = activities + currentActivity

            // determine fixed cost based on cost variant
            val fixedCost =
              variantConfig.fixedActivityCosts.getOrElse(currentActivity, 0.0)

            // extract concrete cost driver nodes and add respective drivers from cost variant config
            var concreteCostDriversOfActivity = Seq[ConcreteCostDriver]();
            eventNode.child.foreach(eventAttribute =>
              if eventAttribute.attribute("key").isDefined && eventAttribute
                  .attribute("key")
                  .get
                  .text == "cost:driver"
              then
                // extract and store cost driver from cost variant config
                val costDriverIdentifier: String =
                  eventAttribute.attribute("value").get.text;
                concreteCostDriversOfActivity =
                  concreteCostDriversOfActivity :+ costVariantOfTrace.costDrivers
                    .find(_.name == costDriverIdentifier)
                    .get
            );
            // store activity profile for trace
            activityProfilesOfTrace = activityProfilesOfTrace :+ ActivityProfile(
              currentActivity,
              concreteCostDriversOfActivity.toList,
              fixedCost
            );
        );
        // create trace profile for trace and its extracted activity profiles
        traceProfiles = traceProfiles :+ TraceProfile(activityProfilesOfTrace.toList);
    )
    
    ParsedLog(activities, traceProfiles.toList)
