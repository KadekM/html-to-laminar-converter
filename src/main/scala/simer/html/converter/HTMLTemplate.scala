package simer.html.converter

import org.scalajs.dom
import org.scalajs.dom.html.Input

import com.raquo.laminar.api.L._

object HTMLTemplate {

  def isNewLineAttributes = !dom.document.getElementById("newlineAttributes").asInstanceOf[Input].checked

  val signal = Var(initial = "nothing")
  val converter = HtmlToTagsConverter.runConverter(LaminarTagsConverter)

  def template = {
    div(
      ul(
        li(
          a(href := "#", "HTML TO LAMINARTAGS CONVERTER")
        ),
        li(float := "center",
          u(
            a(href := "https://simerplaha.github.io/html-to-scalatags-converter/", target := "_blank", "Fork of Simer's work")
          )
        ),
        li(float := "right",
          u(
            a(href := "https://github.com/KadekM/html-to-laminar-converter", target := "_blank", "Report an issue")
          )
        )
      ),
      table(width := "100%",
        tr(width := "100%",
          th(width := "50%", h4("HTML")),
          th(width := "50%", h4("LaminarTags")
          )
        ),
        tr(width := "100%",
          td(width := "50%",
            textArea(idAttr := "htmlCode", cls := "boxsizingBorder", width := "100%", rows := 32, placeholder := "Enter your HTML code here.",
              inContext { thisNode => onInput.map(_ => thisNode.ref.value) --> signal },
              inContext { thisNode => onFocus.map(_ => thisNode.ref.value) --> signal },
              """<div class="myClass">
                |    <div class="someClass" data-attribute="someValue">
                |        <button type="button" class="btn btn-default">Button</button>
                |    </div>
                |    <br/>
                |    <span>
                |       <img class="my-img-class" src="assets/images/image1.jpg" onclick='alert("clicked!");' alt=""/>
                |    </span>
                |    <a href="javascript:void(0);" class="my-class" data-toggle="dropdown">
                |       Some link
                |    </a>
                |    <ul class="dropdown-menu" style="list-style: none; padding: 0;">
                |       <li>
                |           List item 1
                |       </li>
                |       <li>
                |           List&nbsp;item&nbsp;2
                |       </li>
                |    </ul>
                |    <script>
                |       document.getElementById("someId").value = "Hello Scala.js!";
                |    </script>
                |</div>""".stripMargin
            )
          ),
          td(width := "50%",
            textArea(cls := "boxsizingBorder", width := "100%", rows := 32, placeholder := "Scala code will be generated here.", value <-- signal.signal.map(converter))
          )
        ),
        tr(width := "100%",
          td(colSpan := 2, textAlign := "center", paddingTop := "5px",
          )
        )
      )
    )
  }
}
