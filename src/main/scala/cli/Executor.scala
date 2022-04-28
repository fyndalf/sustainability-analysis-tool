package cli

import cost.CostCalculator.{calculateCostDifferences, calculateProcessCost}
import cost.{
  printProcessCost,
  printProcessCostDifference,
  ProcessCost,
  ProcessCostDifference
}
import parser.Log.parseLog
import parser.Model.{highlightCostDifferenceInModel, highlightCostInModel}
import parser.VariantConfig
import parser.VariantConfig.parseCostVariantConfig

import java.nio.file.Path

object Executor:

  def analyseSingleLog(logPath: Path, costConfigPath: Path): ProcessCost =
    val config = parseCostVariantConfig(costConfigPath)
    val log = parseLog(logPath, config)
    val processCost = calculateProcessCost(log)
    printProcessCost(processCost)
    processCost

  def analyseSingleLogAndProcessModel(
      logPath: Path,
      costConfigPath: Path,
      modelPath: Path
  ): Unit =
    val processCost = analyseSingleLog(logPath, costConfigPath)
    highlightCostInModel(processCost, modelPath)

  def analyseTwoLogs(
      logPath: Path,
      costConfigPath: Path,
      secondLogPath: Path,
      secondCostConfigPath: Path,
      isComparisonRelative: Boolean
  ): ProcessCostDifference =
    val firstCost = analyseSingleLog(logPath, costConfigPath)
    val secondCost = analyseSingleLog(secondLogPath, secondCostConfigPath)

    // todo: take mapping of activities before/after into account here
    val costDifference = calculateCostDifferences(
      firstCost,
      secondCost,
      isComparisonRelative
    )

    printProcessCostDifference(costDifference)
    costDifference

  def analyseTwoLogsandProcessModel(
      logPath: Path,
      costConfigPath: Path,
      secondLogPath: Path,
      secondCostConfigPath: Path,
      modelPath: Path,
      isComparisonRelative: Boolean
  ): Unit =
    val processCostDifference = analyseTwoLogs(
      logPath,
      costConfigPath,
      secondLogPath,
      secondCostConfigPath,
      isComparisonRelative
    )
    highlightCostDifferenceInModel(processCostDifference, modelPath)
