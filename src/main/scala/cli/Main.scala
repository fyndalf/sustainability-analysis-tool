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
  analyseTwoLogsandProcessModel
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
            long = "processModel",
            help = "Process Model of the first simulation run"
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
                "Perform a relative comparison of process costs instead of absolute"
            )
            .orFalse

        (
          logFilePath,
          costVariantConfigPath,
          processModelPath,
          secondLogFile,
          secondCostVariantConfig,
          relative
        ).mapN {
          (
              logPathParam,
              costPathParam,
              modelPathParam,
              secondLogPathParam,
              secondCostPathParam,
              isComparisonRelative
          ) =>

            val mode: AnalysisMode = determineAnalysisMode(
              modelPathParam,
              secondLogPathParam,
              secondCostPathParam
            )

            println(mode.toString)

            // todo: comment everything

            // todo: parameter for relative analysis vs absolute

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
                analyseTwoLogsandProcessModel(
                  logPathParam,
                  costPathParam,
                  secondLogPathParam.get,
                  secondCostPathParam.get,
                  modelPathParam.get,
                  isComparisonRelative
                )

        }
    )
