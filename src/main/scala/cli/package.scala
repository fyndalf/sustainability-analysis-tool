import cli.AnalysisMode
import cli.AnalysisMode.*

import java.nio.file.Path

package object cli:

  /** Encapsulates the analysis mode with which the analysis is conducted.
    */
  sealed abstract class AnalysisMode()

  object AnalysisMode:
    final case class SingleLog() extends AnalysisMode()

    final case class SingleLogAndProcessModel() extends AnalysisMode()

    final case class TwoLogs() extends AnalysisMode()

    final case class TwoLogsAndProcessModel() extends AnalysisMode()

  /** Based on the supplied additional parameters, the mode of analysis is
    * determined. Only the required log and config, or an additional process
    * model, or an additional log and config, or an additional log and config
    * and process model are the possible combinations of parameters.
    */
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
