enablePlugins(ScalaJSPlugin)

name := "HtmlToLaminarTagsConverter"

version := "1.0.0"

scalaVersion := "2.13.5"

scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
  "com.raquo" %%% "laminar" % "0.13.0"
)
