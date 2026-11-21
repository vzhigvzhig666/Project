package course.qa.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodosApiTest {
    private val client = HttpClient(CIO) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
        }
    }

    @After
    fun closeClient() = client.close()

    @Test
    fun getReturnsExistingTodo() = runBlocking {
        val response = client.get("$todos/1")
        val body = response.jsonBody(HttpStatusCode.OK)

        assertEquals(1, body.getValue("id").jsonPrimitive.int)
        assertTrue(body.getValue("todo").jsonPrimitive.content.isNotBlank())
    }

    @Test
    fun getRejectsMissingTodo() = runBlocking {
        val response = client.get("$todos/$missingId")
        assertMissingTodo(response)
    }

    @Test
    fun postReturnsCreatedTodo() = runBlocking {
        val title = "Prepare UI tests"
        val response = client.post("$todos/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"todo":"$title","completed":false,"userId":1}""")
        }
        val body = response.jsonBody(HttpStatusCode.Created)

        assertTrue(body.getValue("id").jsonPrimitive.int > 0)
        assertEquals(title, body.getValue("todo").jsonPrimitive.content)
        assertEquals(1, body.getValue("userId").jsonPrimitive.int)
        assertEquals(false, body.getValue("completed").jsonPrimitive.boolean)
    }

    @Test
    fun postRejectsMalformedJson() = runBlocking {
        val response = client.post("$todos/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"todo":""")
        }
        val body = response.jsonBody(HttpStatusCode.BadRequest)

        assertTrue(body.getValue("message").jsonPrimitive.content.isNotBlank())
    }

    @Test
    fun patchReturnsUpdatedCompletionStatus() = runBlocking {
        val response = client.patch("$todos/1") {
            contentType(ContentType.Application.Json)
            setBody("""{"completed":true}""")
        }
        val body = response.jsonBody(HttpStatusCode.OK)

        assertEquals(1, body.getValue("id").jsonPrimitive.int)
        assertTrue(body.getValue("completed").jsonPrimitive.boolean)
    }

    @Test
    fun patchRejectsMissingTodo() = runBlocking {
        val response = client.patch("$todos/$missingId") {
            contentType(ContentType.Application.Json)
            setBody("""{"completed":true}""")
        }
        assertMissingTodo(response)
    }

    private suspend fun assertMissingTodo(response: HttpResponse) {
        val body = response.jsonBody(HttpStatusCode.NotFound)
        assertTrue(body.getValue("message").jsonPrimitive.content.contains(missingId.toString()))
    }

    private suspend fun HttpResponse.jsonBody(expectedStatus: HttpStatusCode): JsonObject {
        val body = bodyAsText()
        assertEquals("Response body: $body", expectedStatus, status)
        assertTrue(contentType()?.match(ContentType.Application.Json) == true)
        return Json.parseToJsonElement(body).jsonObject
    }

    private companion object {
        const val todos = "https://dummyjson.com/todos"
        const val missingId = 999_999
    }
}
