package parser

import cost.{ActivityIdentifier, ConcreteCostDriver}

import java.nio.file.Path
import scala.collection
import scala.collection.Seq
import scala.collection.immutable.HashMap
import scala.language.postfixOps
import scala.xml.XML

object VariantConfig:
  def parseCostVariantConfig(path: Path): CostVariantConfig =
    val variantXML = XML.loadFile(path.toFile)

    var variants = Seq[CostVariant]()
    var costMap = new HashMap[ActivityIdentifier, Double]()

    variantXML.child
      .foreach(c =>
        if c.label == "variant" then
          val costDrivers: Seq[ConcreteCostDriver] = c.child
            .filter(_.label == "driver")
            .map(d =>
              ConcreteCostDriver(
                d.attribute("id").get.text,
                d.attribute("cost").get.text.toDouble
              )
            )
          variants = variants :+ CostVariant(
            c.attribute("id").get.text,
            costDrivers.toList
          )
        if c.label == "fixed_cost" then
          costMap = costMap + (ActivityIdentifier(
            c.attribute("id").get.text
          ) -> c.attribute("cost").get.text.toDouble)
      )

    CostVariantConfig(variants.toList, costMap)
