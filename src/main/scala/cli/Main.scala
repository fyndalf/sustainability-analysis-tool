package cli

import cats.implicits.*
import cli.AnalysisMode.{
  SingleLog,
  SingleLogAndProcessModel,
  TwoLogs,
  TwoLogsAndProcessModel
}
import cli.Executor.{
  analyseSingleLog,
  analyseSingleLogAndProcessModel,
  analyseTwoLogs,
  analyseTwoLogsAndProcessModel
}
import com.monovore.decline.*

import java.io.FileNotFoundException
import java.nio.file.Path
import scala.language.implicitConversions

/** The main object of the CLI tool - it is used to run the tool. Based on the
  * passed parameters, the analysis mode is determined.
  */
object Main
    extends CommandApp(
      name = "sustainability-analysis-tool",
      header = "Analyse cost-driver enriched event logs and processes",
      main =

        val logFilePath = Opts.argument[Path](metavar = "log-file")
        val costVariantConfigPath =
          Opts.argument[Path](metavar = "cost-variant-config")
        val processModelPath = Opts
          .option[Path](
            long = "process-model",
            help = "Process Model of the first process execution"
          )
          .orNone
        val secondLogFile = Opts
          .option[Path](
            long = "second-log",
            help = "Second log file after re-design"
          )
          .orNone
        val secondCostVariantConfig = Opts
          .option[Path](
            long = "second-config",
            help = "Second log file after re-design"
          )
          .orNone

        val relative =
          Opts
            .flag(
              "relative",
              help =
                "Perform a relative comparison of process costs instead of absolute comparison."
            )
            .orFalse

        val averageDifference =
          Opts
            .flag(
              "average_difference",
              help =
                "Perform a difference analysis based on average differences. If not, it is done in relation to a difference of 0."
            )
            .orFalse

        (
          logFilePath,
          costVariantConfigPath,
          processModelPath,
          secondLogFile,
          secondCostVariantConfig,
          relative,
          averageDifference
        ).mapN {
          (
              logPathParam,
              costPathParam,
              modelPathParam,
              secondLogPathParam,
              secondCostPathParam,
              isComparisonRelative,
              isComparisonBasedOnAverage
          ) =>

            val mode: AnalysisMode = determineAnalysisMode(
              modelPathParam,
              secondLogPathParam,
              secondCostPathParam
            )

            mode match
              case _: SingleLog => println("Analyzing a single event log...")
              case _: SingleLogAndProcessModel =>
                println(
                  "Analyzing a single event log and highlighting results in a process model ..."
                )
              case _: TwoLogs => println("Analyzing two event logs...")
              case _: TwoLogsAndProcessModel =>
                println(
                  "Analyzing two event logs and highlighting results in a process model ..."
                )

            mode match
              case _: SingleLog => analyseSingleLog(logPathParam, costPathParam)
              case _: SingleLogAndProcessModel =>
                analyseSingleLogAndProcessModel(
                  logPathParam,
                  costPathParam,
                  modelPathParam.get
                )
              case _: TwoLogs =>
                analyseTwoLogs(
                  logPathParam,
                  costPathParam,
                  secondLogPathParam.get,
                  secondCostPathParam.get,
                  isComparisonRelative
                )
              case _: TwoLogsAndProcessModel =>
                analyseTwoLogsAndProcessModel(
                  logPathParam,
                  costPathParam,
                  secondLogPathParam.get,
                  secondCostPathParam.get,
                  modelPathParam.get,
                  isComparisonRelative,
                  isComparisonBasedOnAverage
                )

        }
    )
