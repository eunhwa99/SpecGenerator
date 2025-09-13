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
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.http.HttpDocumentation.httpResponse
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.snippet.Attributes.key
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(ItemController::class) // @WebMvcTest는 그대로 유지
@AutoConfigureRestDocs(outputDir = "build/generated-snippets") // @AutoConfigureRestDocs도 그대로
@ExtendWith(RestDocumentationExtension::class) // Rest Docs 확장을
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
            fieldWithPath("[].id").description("아이템 ID"),
            fieldWithPath("[].name").description("아이템 이름"),
            fieldWithPath("[].price").description("아이템 가격"),
            fieldWithPath("[].createdAt").description("생성 일시").optional(),
            fieldWithPath("[].updatedAt").description("수정 일시").optional()
        )

        val itemResponseField = responseFields(
            fieldWithPath("id").description("아이템 ID"),
            fieldWithPath("name").description("아이템 이름"),
            fieldWithPath("price").description("아이템 가격"),
            fieldWithPath("createdAt").description("생성 일시").optional(),
            fieldWithPath("updatedAt").description("수정 일시").optional()
        )

        val createItemRequestFields = requestFields().apply {
            fieldWithPath("name")
                .description("아이템 이름 (필수, 1-100자)")
                .attributes(key("constraints").value("Required, 1-100 characters"))
            fieldWithPath("price")
                .description("아이템 가격 (필수, 0보다 큰 값)")
                .attributes(key("constraints").value("Positive number"))
        }

        val itemRequestBody = relaxedRequestFields(
            fieldWithPath("name")
                .description("아이템 이름 (필수, 1-100자)")
                .attributes(
                    key("type").value("String"),
                    key("constraints").value("Required, 1-100 characters"),
                    key("optional").value(false)
                ),
            fieldWithPath("price")
                .description("아이템 가격 (필수, 0보다 큰 값)")
                .attributes(
                    key("type").value("Integer"),
                    key("constraints").value("Positive number"),
                    key("optional").value(true)
                )
        )

        val errorFields = responseFields(
            fieldWithPath("message").description("에러 메시지"),
            fieldWithPath("errors[]").description("상세 에러 목록"),
            fieldWithPath("errors[].field").description("에러 필드"),
            fieldWithPath("errors[].message").description("필드별 에러 메시지"),
            fieldWithPath("timestamp").description("에러 발생 시각")
        )
    }

    // 헬퍼 함수: 반복되는 document 호출 간소화
    private fun documentAuto(snippetName: String, vararg extraSnippets: Snippet) =
        document(
            snippetName,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            *extraSnippets
        )

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .snippets()
                    .withDefaults(
                        requestHeaders(),
                        curlRequest(),
                        httpResponse(),
                    )

            )
            .build()
    }

    @Test
    fun `POST create item 400 - 잘못된 요청`() {
        val invalid = """{"name":"","price":-100}"""
        mockMvc.post("/api/items") {
            contentType = MediaType.APPLICATION_JSON
            content = invalid
        }
            .andExpect { status { isBadRequest() } }
            .andDo { documentAuto("items/create-error", errorFields) }
    }

    @Test
    fun `GET items 200 - 모든 아이템 조회`() {
        mockMvc.perform(
            get("/api/items").accept(MediaType.APPLICATION_JSON).header("X-Custom-Header", "test")
        )
            .andExpect(status().isOk)
            .andDo(
                documentAuto(
                    "items/get-all",
                    requestHeaders(
                        headerWithName("X-Custom-Header")
                            .description("커스텀 헤더")
                            .attributes(
                                key("type").value("String"),
                                key("constraints").value("필수"),
                                key("optional").value("false")
                            )
                    ),
                    itemResponseFields
                )
            )
    }

    @Test
    fun `GET item by ID 200 - 특정 아이템 조회`() {
        mockMvc.perform(get("/api/items/{id}", 1).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andDo(
                documentAuto(
                    "items/get-by-id",
                    pathParameters(parameterWithName("id").description("조회할 아이템의 ID")),
                    itemResponseField
                )
            )
    }

    @Test
    fun `GET items with query params 200 - 쿼리 파라미터로 아이템 검색`() {
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
    fun `POST create item 201 - 새 아이템 생성`() {
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
                    "items/create",
                    itemRequestBody
                )
            )
    }

    @Test
    fun `POST create item 400 - Bad Request`() {
        val request = ItemRequest("12345678910101010", 1500)
        val json = objectMapper.writeValueAsString(request)

        mockMvc.perform(
            post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andDo(documentAuto("items/create-error"))
    }


    @Test
    fun `PUT update item 200 - 아이템 정보 수정`() {
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
                    "items/update",
                    pathParameters(parameterWithName("id").description("수정할 아이템의 ID")),
                    itemRequestBody,
                )
            )
    }

    @Test
    fun `DELETE item 204 - 아이템 삭제`() {
        mockMvc.perform(delete("/api/items/{id}", 1))
            .andExpect(status().isNoContent)
            .andDo(
                documentAuto(
                    "items/delete",
                    pathParameters(parameterWithName("id").description("삭제할 아이템의 ID"))
                )
            )
    }
}
