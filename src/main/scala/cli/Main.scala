package cli

import scala.language.implicitConversions
import com.monovore.decline.*
import cats.implicits.*
import cli.AnalysisMode.{SingleLog, SingleLogAndProcessModel, TwoLogs, TwoLogsAndProcessModel}

import java.io.FileNotFoundException
import java.nio.file.Path

/**
 * The main object of the CLI tool - it is used to run the tool.
 */
object Main
  extends CommandApp(
    name = "sustainability-analysis-tool",
    header = "Analyse cost-driver enriched event logs and processes",
    main = {

      val logFilePath = Opts.argument[Path](metavar = "log-file")
      val costVariantConfigPath = Opts.argument[Path](metavar = "cost-variant-config")
      val processModelPath = Opts.option[Path](
        long = "processModel",
        help = "Process Model of the first simulation run"
      ).orNone
      val secondLogFile = Opts.option[Path](
        long = "second-log",
        help = "Second log file after re-design"
      ).orNone
      val secondCostVariantConfig = Opts.option[Path](
        long = "second-cost-variant-config",
        help = "Second log file after re-design"
      ).orNone

      (
        logFilePath,
        costVariantConfigPath,
        processModelPath,
        secondLogFile,
        secondCostVariantConfig,
        ).mapN {
        (
          logPathParam,
          costPathParam,
          modelPathParam,
          secondLogPathParam,
          secondCostPathParam
        ) =>

          val mode: AnalysisMode = determineAnalysisMode(modelPathParam, secondLogPathParam , secondCostPathParam)

          mode match
            case _: SingleLog => ???
            case _: SingleLogAndProcessModel => ???
            case _: TwoLogs => ???
            case _: TwoLogsAndProcessModel => ???


          println(mode.toString)

          val resultPath = "somePath"
          println(s"Done.\nThe event log is stored in $resultPath ")

      }
    }
  )
