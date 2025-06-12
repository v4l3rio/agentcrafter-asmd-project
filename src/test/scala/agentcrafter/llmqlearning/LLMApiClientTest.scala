package agentcrafter.llmqlearning

import agentcrafter.llmqlearning.LLMHttpClient
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Failure


class LLMApiClientTest extends AnyFunSuite with Matchers:

  test("LLMApiClient should handle invalid base URL gracefully"):
    val client = LLMHttpClient(baseUrl = "https://nonexistent-domain-12345.com")
    
    // The client should be created but API calls should fail
    val result = client.callLLM("test prompt")
    result shouldBe a[Failure[?]]
    result.failed.get.getMessage should include("Error calling")