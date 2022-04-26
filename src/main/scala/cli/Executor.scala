package cli

import cost.CostCalculator.{calculateCostDifferences, calculateProcessCost}
import cost.{printProcessCost, printProcessCostDifference}
import parser.Log.parseLog
import parser.Model.highlightCostInModel
import parser.VariantConfig
import parser.VariantConfig.parseCostVariantConfig

import java.nio.file.Path

object Executor:

  def analyseSingleLog(logPath: Path, costConfigPath: Path): Unit =
    val config = parseCostVariantConfig(costConfigPath)
    val log = parseLog(logPath, config)
    val processCost = calculateProcessCost(log)
    printProcessCost(processCost)

  def analyseTwoLogs(
      logPath: Path,
      costConfigPath: Path,
      secondLogPath: Path,
      secondCostConfigPath: Path
  ): Unit =
    val firstConfig = parseCostVariantConfig(costConfigPath)
    val firstLog = parseLog(logPath, firstConfig)
    val firstCost = calculateProcessCost(firstLog)

    val secondConfig = parseCostVariantConfig(secondCostConfigPath)
    val secondLog = parseLog(secondLogPath, secondConfig)
    val secondCost = calculateProcessCost(secondLog)

    val costDifference = calculateCostDifferences(firstCost, secondCost)

    printProcessCost(firstCost)
    printProcessCost(secondCost)
    printProcessCostDifference(costDifference)

  def analyseSingleLogAndProcessModel(
      logPath: Path,
      costConfigPath: Path,
      modelPath: Path
  ): Unit =
    val config = parseCostVariantConfig(costConfigPath)
    val log = parseLog(logPath, config)
    val processCost = calculateProcessCost(log)
    printProcessCost(processCost)

    highlightCostInModel(processCost, modelPath)
