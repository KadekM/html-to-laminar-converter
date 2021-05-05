package simer.html.converter


sealed trait ConverterType {
  val attributePrefix: String
  val nodePrefix: String
  val customAttributePostfix: String
  val classAttributeKey: String
  val newLineAttributes: Boolean

  val renames: Map[String, String]
}

case object LaminarTagsConverter extends ConverterType {
  val attributePrefix: String = ""
  val nodePrefix: String = ""
  val customAttributePostfix: String = "attr"
  val classAttributeKey: String = "cls"
  val newLineAttributes: Boolean = false

  val renames: Map[String, String] = Map(
    "class" -> "cls",
    "for" -> "forId"
  )
}
