package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.cli.CliDocumentation.curlRequest
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.http.HttpDocumentation.httpResponse
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.payload.RequestFieldsSnippet
import org.springframework.restdocs.request.PathParametersSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(ItemController::class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@ExtendWith(RestDocumentationExtension::class)
class ItemControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    companion object {
        // 공통 Request/Response 필드 템플릿
        val itemResponseFields = responseFields(
            bodyField("[].id", "아이템 ID", "Long", "Generated ID"),
            bodyField(path = "[].name", description = "아이템 이름", constraints = "1-100 characters"),
            bodyField(
                path = "[].price",
                description = "아이템 가격",
                type = "Integer",
                constraints = "Positive number"
            ),
            bodyField(
                path = "[].createdAt",
                description = "생성 일시",
                "LocalDateTime",
                "",
                optional = true
            ),
            bodyField(
                path = "[].updatedAt",
                description = "수정 일시",
                "LocalDateTime",
                "",
                optional = true
            )
        )


        val itemResponseField = responseFields(
            bodyField("id", "아이템 ID", "Long", "Generated ID"),
            bodyField(path = "name", description = "아이템 이름", constraints = "1-100 characters"),
            bodyField(
                "price", description = "아이템 가격",
                type = "Integer",
                constraints = "Positive number"
            ),
            bodyField(
                "createdAt", description = "생성 일시",
                "LocalDateTime",
                "",
                optional = true
            ),
            bodyField(
                "updatedAt", description = "수정 일시",
                "LocalDateTime",
                "",
                optional = true
            )
        )

        // ------------------ Request Fields ------------------
        val itemRequestBody: RequestFieldsSnippet = requestFields(
            bodyField(
                "name",
                "아이템 이름 (필수, 1-100자)",
                "String",
                "Required, 1-100 characters",
                optional = false
            ),
            bodyField(
                "price",
                "아이템 가격 (필수, 0보다 큰 값)",
                "Integer",
                "Positive number",
                optional = true
            )
        )

        val errorFields = responseFields(
            fieldWithPath("timestamp").description("에러 발생 시각"),
            fieldWithPath("status").description("HTTP 상태 코드"),
            fieldWithPath("error").description("에러 타입"),
            fieldWithPath("message").description("에러 메시지"),
            fieldWithPath("path").description("요청 경로")
        )

        val headers = requestHeaders(
            headerField("x-custom-header", "커스텀 헤더", "String", "필수", false)
        )
        val pathParams: PathParametersSnippet = pathParameters(
            pathParam(
                "id", "아이템 ID", "Long", "Required", true
            )
        )
    }

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .snippets()
                    .withDefaults(
                        curlRequest(),
                        httpResponse()
                    )
            )
            .build()
    }

    private fun documentAuto(snippetName: String, vararg extraSnippets: Snippet) =
        document(
            snippetName,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            *extraSnippets
        )
    // ============= CREATE ITEM =============

    @Test
    fun `POST create item - Common Request Info`() {
        val request = ItemRequest("새로운 아이템", 1500)
        val json = objectMapper.writeValueAsString(request)

        mockMvc.perform(
            post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isCreated)
            .andDo(
                documentAuto(
                    "items/create", // 기본 요청 정보
                    itemRequestBody
                )
            )
    }

    @Test
    fun `POST create item - 201 Success Response`() {
        val request = ItemRequest("새로운 아이템", 1500)
        val json = objectMapper.writeValueAsString(request)

        mockMvc.perform(
            post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isCreated)
            .andDo(
                documentAuto(
                    "items/create/201-response",
                    itemRequestBody
                )
            )
    }

    @Test
    fun `POST create item - 400 Bad Request Response`() {
        val invalidJson = """{"name":"","price":-100}"""

        mockMvc.perform(
            post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andDo(
                documentAuto(
                    "items/create/400-response",
                    itemRequestBody,
                )
            )
    }

    // ============= GET ALL ITEMS =============

    @Test
    fun `GET items - Common Request Info`() {
        mockMvc.perform(
            get("/api/items")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Custom-Header", "test")
        )
            .andExpect(status().isOk)
            .andDo(
                documentAuto(
                    "items/get-all",
                    headers,
                    itemResponseFields
                )
            )
    }

    // ============= GET ITEM BY ID =============

    @Test
    fun `GET item by ID - Common Request Info`() {
        mockMvc.perform(
            get("/api/items/{id}", 1)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andDo(
                documentAuto(
                    "items/get-by-id",
                    pathParams
                )
            )
    }


    @Test
    fun `GET item by ID - 404 Not Found Response`() {
        mockMvc.perform(
            get("/api/items/{id}/00", 999)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andDo(
                documentAuto(
                    "items/get-by-id/404-response",
                )
            )
    }

    // ============= SEARCH ITEMS =============

    @Test
    fun `GET items with query params - Common Request Info`() {
        mockMvc.perform(
            get("/api/items")
                .param("name", "item")
                .param("minPrice", "100")
                .param("maxPrice", "500")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andDo(
                documentAuto(
                    "items/search",
                    pathParameters().apply {
                        parameterWithName("name").description("아이템 이름 검색 키워드").optional()
                        parameterWithName("minPrice").description("최소 가격").optional()
                        parameterWithName("maxPrice").description("최대 가격").optional()
                        parameterWithName("page").description("페이지 번호 (0부터 시작)").optional()
                        parameterWithName("size").description("페이지 크기").optional()
                    }
                )
            )
    }

    @Test
    fun `GET items with query params - 200 Success Response`() {
        mockMvc.perform(
            get("/api/items")
                .param("name", "item")
                .param("minPrice", "100")
                .param("maxPrice", "500")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andDo(
                documentAuto(
                    "items/search/200-response",
                    itemResponseFields
                )
            )
    }


    @Test
    fun `PUT update item - 200 Success Response`() {
        val request = ItemRequest("수정된 아이템", 2000)
        val json = objectMapper.writeValueAsString(request)

        mockMvc.perform(
            put("/api/items/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isOk)
            .andDo(
                documentAuto(
                    "items/update/200-response",
                )
            )
    }

    @Test
    fun `PUT update item - 404 Not Found Response`() {
        val request = ItemRequest("수정된 아이템", 2000)
        val json = objectMapper.writeValueAsString(request)

        mockMvc.perform(
            put("/api/items/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isNotFound)
            .andDo(
                documentAuto(
                    "items/update/404-response",
                )
            )
    }

    // ============= DELETE ITEM =============

    @Test
    fun `DELETE item - Common Request Info`() {
        mockMvc.perform(delete("/api/items/{id}", 1))
            .andExpect(status().isNoContent)
            .andDo(
                documentAuto(
                    "items/delete",
                    pathParams
                )
            )
    }

    @Test
    fun `DELETE item - 204 No Content Response`() {
        mockMvc.perform(delete("/api/items/{id}", 1))
            .andExpect(status().isNoContent)
            .andDo(
                documentAuto(
                    "items/delete/204-response",
                )
            )
    }

    @Test
    fun `DELETE item - 404 Not Found Response`() {
        mockMvc.perform(delete("/api/items/{id}/11", 999))
            .andExpect(status().isNotFound)
            .andDo(
                documentAuto(
                    "items/delete/404-response"
                )
            )
    }

    // Data classes
    data class ItemRequest(val name: String, val price: Int)
}