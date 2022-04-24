package cli

import parser.VariantConfig
import parser.VariantConfig.parseCostVariantConfig
import parser.Log.parseLog

import java.nio.file.Path

object Executor {

  def analyseSingleLog(logPath: Path, costConfigPath: Path): Unit = {
    val config = parseCostVariantConfig(costConfigPath)
    val log = parseLog(logPath, config)

  }

}
