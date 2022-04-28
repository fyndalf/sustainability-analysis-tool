package parser

import cost.{ActivityIdentifier, ConcreteCostDriver}

import java.nio.file.Path
import scala.collection
import scala.collection.Seq
import scala.collection.immutable.HashMap
import scala.language.postfixOps
import scala.xml.XML

object VariantConfig:
  /** Parses a cost variant config file from a path.
    */
  def parseCostVariantConfig(path: Path): CostVariantConfig =
    val variantXML = XML.loadFile(path.toFile)

    var variants = Seq[CostVariant]()

    // store assignment of fixed costs
    var fixedCostMap = new HashMap[ActivityIdentifier, Double]()

    variantXML.child
      .foreach(variantNode =>
        if variantNode.label == "variant" then
          // extract concrete cost drivers for variant
          val costDrivers: Seq[ConcreteCostDriver] = variantNode.child
            .filter(_.label == "driver")
            .map(driverNode =>
              ConcreteCostDriver(
                driverNode.attribute("id").get.text,
                driverNode.attribute("cost").get.text.toDouble
              )
            );
          // store variant with drivers
          variants = variants :+ CostVariant(
            variantNode.attribute("id").get.text,
            costDrivers.toList
          )
        if variantNode.label == "fixed_cost" then
          // store fixed costs for activity
          fixedCostMap = fixedCostMap + (ActivityIdentifier(
            variantNode.attribute("id").get.text
          ) -> variantNode.attribute("cost").get.text.toDouble)
      )

    CostVariantConfig(variants.toList, fixedCostMap)
