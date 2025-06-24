package agentcrafter.llmqlearning

import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.client4.{Response, quickRequest}
import sttp.model.Uri

import scala.concurrent.duration.Duration

trait SimpleHttpClient:
  def post(uri: Uri, body: String, apiKey: String): Response[String]

object SimpleHttpClient:
  def apply(): SimpleHttpClient = DefaultSimpleHttpClient()

private class DefaultSimpleHttpClient extends SimpleHttpClient:
  override def post(uri: Uri, body: String, apiKey: String): Response[String] =
    quickRequest
      .post(uri)
      .contentType("application/json")
      .header("Authorization", s"Bearer $apiKey")
      .body(body)
      .readTimeout(Duration.Inf)
      .send(HttpClientSyncBackend())
