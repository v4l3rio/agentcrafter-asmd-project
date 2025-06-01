package llmqlearning

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.util.{Success, Failure}
import java.io.File
import java.nio.file.{Files, Paths}

class LLMApiClientTest extends AnyFunSuite with Matchers:

  test("LLMApiClient should handle invalid base URL gracefully"):
    val client = LLMApiClient(baseUrl = "https://nonexistent-domain-12345.com")
    
    // The client should be created but API calls should fail
    val result = client.callLLM("test prompt")
    result shouldBe a[Failure[_]]
    result.failed.get.getMessage should include("Error calling")