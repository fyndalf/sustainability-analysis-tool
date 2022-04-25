package parser

import cost.{ActivityIdentifier, ActivityProfile, ConcreteCostDriver, TraceProfile}
import parser.VariantConfig

import java.nio.file.Path
import scala.xml.XML

object Log:

  def parseLog(path: Path, variantConfig: CostVariantConfig): ParsedLog =
    val logXML = XML.loadFile(path.toFile)

    var traceProfiles = Seq[TraceProfile]()
    var activities = Set[ActivityIdentifier]()

    // todo: improve naming

    logXML.child.foreach(c => {
      if c.label == "trace" then {

        var activityProfiles = Seq[ActivityProfile]()
        val costVariantIdentifier = c.child.find(n => n.attribute("key").isDefined && n.attribute("key").get.text == "cost:variant").get.attribute("value").get.text
        val currentCostVariant = variantConfig.variants.find(_.id == costVariantIdentifier).get
        c.child.foreach(n => {
          if n.label == "event" then {
            val currentActivityIdentifier = n.child.find(m => m.attribute("key").isDefined && m.attribute("key").get.text == "concept:name").get.attribute("value").get.text
            val currentActivity = ActivityIdentifier(currentActivityIdentifier)
            activities = activities + currentActivity
            val fixedCost = variantConfig.fixedActivityCosts.getOrElse(currentActivity, 0.0)

            var concreteCostDrivers = Seq[ConcreteCostDriver]()
            n.child.foreach(eventAttribute => {
              if eventAttribute.attribute("key").isDefined && eventAttribute.attribute("key").get.text == "cost:driver" then {
                val costDriverIdentifier = eventAttribute.attribute("value").get.text
                concreteCostDrivers = concreteCostDrivers :+ currentCostVariant.costDrivers.find(_.name == costDriverIdentifier).get
              }
            })
            activityProfiles = activityProfiles :+ ActivityProfile(
              currentActivity,
              concreteCostDrivers.toList,
              fixedCost
            )
          }
        })
        traceProfiles = traceProfiles :+ TraceProfile(activityProfiles.toList)
      }
    })
    ParsedLog(activities, traceProfiles.toList)

