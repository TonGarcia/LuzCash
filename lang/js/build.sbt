scalaJSLinkerConfig ~= {
  _.withModuleKind(ModuleKind.CommonJSModule)
}

lazy val circe = Def.setting {
  val circeVersion = "0.13.0"
  Seq(
    "io.circe" %%% "circe-core",
    "io.circe" %%% "circe-generic",
    "io.circe" %%% "circe-parser"
  ).map(_ % circeVersion)
}

libraryDependencies ++= circe.value
