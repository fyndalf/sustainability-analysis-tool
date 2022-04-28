package cli

import cost.{ProcessCost, ProcessCostDifference}
import cost.CostCalculator.{calculateCostDifferences, calculateProcessCost}
import cost.printer.{printProcessCost, printProcessCostDifference}
import parser.Log.parseLog
import parser.Model.{highlightCostDifferenceInModel, highlightCostInModel}
import parser.VariantConfig
import parser.VariantConfig.parseCostVariantConfig

import java.nio.file.Path

object Executor:

  /** Analyses and prints the cost of a single log and config.
    */
  def analyseSingleLog(logPath: Path, costConfigPath: Path): ProcessCost =
    val config = parseCostVariantConfig(costConfigPath)
    val log = parseLog(logPath, config)
    val processCost = calculateProcessCost(log)
    printProcessCost(processCost)
    processCost

  /** Analyses, prints, and visualizes the cost of a single log and config.
    */
  def analyseSingleLogAndProcessModel(
      logPath: Path,
      costConfigPath: Path,
      modelPath: Path
  ): Unit =
    val processCost = analyseSingleLog(logPath, costConfigPath)
    highlightCostInModel(processCost, modelPath)

  /** Analyses and prints the cost difference between two logs and configs
    */
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

  /** Analyses, prints, and visualizes the cost difference between two logs and
    * configs, using a process model.
    */
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
