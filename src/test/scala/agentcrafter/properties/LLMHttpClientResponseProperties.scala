package agentcrafter.properties

import agentcrafter.llmqlearning.{LLMHttpClient, SimpleHttpClient}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers
import sttp.client4.Response
import sttp.model.{Method, RequestMetadata, Uri}

import scala.util.{Failure, Success}

/** Verifies LLMHttpClient’s robustness when the assistant returns rich, nested JSON. */
object LLMHttpClientResponseProperties
    extends Properties("LLMHttpClientResponse") with Matchers:
  
  private val nestedJsonGen: Gen[String] =
    for
      agent <- Gen.identifier.map(_.take(12))
      row <- Gen.choose(0, 9)
      col <- Gen.choose(0, 9)
      up <- Gen.choose(-1.0, 1.0)
      down <- Gen.choose(-1.0, 1.0)
      left <- Gen.choose(-1.0, 1.0)
      right <- Gen.choose(-1.0, 1.0)
      stay <- Gen.choose(-1.0, 1.0)
    yield s"""{"$agent":{"($row,$col)":{"Up":$up,"Down":$down,"Left":$left,"Right":$right,"Stay":$stay}}}"""
  
  private val validGen: Gen[(String, String)] =
    nestedJsonGen.map { inner =>
      val outer =
        s"""{"choices":[{"message":{"content":"${jsonEscape(inner)}"}}]}"""
      (outer, inner)
    }
  private val invalidGen: Gen[String] = Gen.oneOf(
    "not json",
    "{",
    "{}",
    """{"choices":[]}""",
    """{"choices":[{"message":{}}]}""", // missing content
    """{"choices":[{"message":{"content":3}}]}""" // wrong type
  )
  private val responseGen: Gen[(Boolean, String, String)] =
    Gen.frequency(
      5 -> validGen.map { case (o, i) => (true, o, i) },
      5 -> invalidGen.map(badBody => (false, badBody, ""))
    )

  private def jsonEscape(s: String): String =
    s.flatMap {
      case '"' => "\\\""
      case '\\' => "\\\\"
      case c => c.toString
    }

  property("parses nested-JSON content correctly") = forAll(responseGen) {
    case (shouldSucceed, body, expectedInner) =>
      // mock HTTP layer
      val mockHttp = Mockito.mock(classOf[SimpleHttpClient])
      val uri = Uri.parse("https://api.test.com/v1/chat/completions").getOrElse(
        throw new IllegalArgumentException("Invalid URI")
      )
      val metadata = RequestMetadata(Method.POST, uri, Seq.empty)
      val resp = Response.ok(body, metadata)
      Mockito.when(
        mockHttp.post(
          ArgumentMatchers.any(classOf[Uri]),
          ArgumentMatchers.any(classOf[String]),
          ArgumentMatchers.any(classOf[String])
        )
      ).thenReturn(resp)

      val client = LLMHttpClient(
        baseUrl = "https://api.test.com",
        apiKey = "test",
        httpClient = mockHttp
      )
      
      val result = client.callLLMWithContent(
        "prompt",
        "gpt-4o", 
        "simulation: {}", 
        false, 
        "/v1/chat/completions"
      )

      if shouldSucceed then result == Success(expectedInner)
      else result.isFailure
  }
