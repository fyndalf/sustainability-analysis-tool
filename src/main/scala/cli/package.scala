import cli.AnalysisMode
import cli.AnalysisMode.*

import java.nio.file.Path

package object cli:

  sealed abstract class AnalysisMode()

  object AnalysisMode:
    final case class SingleLog() extends AnalysisMode()

    final case class SingleLogAndProcessModel() extends AnalysisMode()

    final case class TwoLogs() extends AnalysisMode()

    final case class TwoLogsAndProcessModel() extends AnalysisMode()

  def determineAnalysisMode(
      modelParam: Option[Path],
      secondLogParam: Option[Path],
      secondCostParam: Option[Path]
  ): AnalysisMode =
    if modelParam.isEmpty && secondLogParam.isEmpty && secondCostParam.isEmpty
    then SingleLog()
    else if modelParam.isDefined && secondLogParam.isEmpty && secondCostParam.isEmpty
    then SingleLogAndProcessModel()
    else if modelParam.isEmpty && secondLogParam.isDefined && secondCostParam.isDefined
    then TwoLogs()
    else if modelParam.isDefined && secondLogParam.isDefined && secondCostParam.isDefined
    then TwoLogsAndProcessModel()
    else SingleLog()
