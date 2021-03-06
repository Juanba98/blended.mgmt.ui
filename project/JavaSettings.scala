import sbt._
import sbt.Keys._
import phoenix.ProjectConfig
import de.wayofquality.sbt.testlogconfig.TestLogConfig
import TestLogConfig.autoImport._
import sbt.internal.inc.Analysis
import xsbti.api.{AnalyzedClass, Projection}
import sbt.Tests.{Group, SubProcess}

trait JavaSettings extends ProjectConfig {
  
  private def hasForkAnnotation(clazz: AnalyzedClass): Boolean = {

    val c = clazz.api().classApi()

    c.annotations.exists { ann =>
      ann.base() match {
        case proj: Projection if proj.id() == "RequiresForkedJVM" => true
        case _ => false
      }
    }
  }

  override def settings: Seq[sbt.Setting[_]] = super.settings ++ Seq(

    Test / javaOptions += ("-DprojectTestOutput=" + (Test / classDirectory).value),
  Test / fork := true,
  Test / parallelExecution := false,
  Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "binaryResources",
  Test / unmanagedResourceDirectories += baseDirectory.value / "src" / "test" / "binaryResources",

  Test / testlogDirectory := (Global/testlogDirectory).value,
  Test / testlogLogToConsole := false,
  Test / testlogLogToFile := true,

  Test / resourceGenerators += (Test / testlogCreateConfig).taskValue,

  // inspired by : https://chariotsolutions.com/blog/post/sbt-group-annotated-tests-run-forked-jvms
  Test / testGrouping := {

    val log = streams.value.log

    val options = (Test / javaOptions).value.toVector

    val annotatedTestNames: Seq[String] = (Test / compile).value.asInstanceOf[Analysis]
      .apis.internal.values.filter(hasForkAnnotation).map(_.name()).toSeq

    val (forkedTests, otherTests) = (Test / definedTests).value.partition { t =>
      annotatedTestNames.contains(t.name)
    }

    val combined: Tests.Group = Group(
      name = "Combined",
      tests = otherTests,
      runPolicy = SubProcess(config = ForkOptions.apply().withRunJVMOptions(options))
    )

    val forked: Seq[Tests.Group] = forkedTests.map { t =>
      Group(
        name = t.name,
        tests = Seq(t),
        runPolicy = SubProcess(config = ForkOptions.apply().withRunJVMOptions(options))
      )
    }

    if (forkedTests.nonEmpty) {
      log.info(s"Forking extra JVM for test [${annotatedTestNames.mkString(",")}]")
    }

    forked ++ Seq(combined)
  }
  )
}
