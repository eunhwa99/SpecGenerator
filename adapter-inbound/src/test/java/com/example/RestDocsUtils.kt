package com.example

import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.restdocs.snippet.Attributes.key

// --------- Body Fields DSL ---------
data class DocField(
    val path: String,
    val description: String,
    val type: String,
    val constraints: String,
    val optional: Boolean = false
) {
    fun toFieldDescriptor(): FieldDescriptor =
        fieldWithPath(path)
            .description(description)
            .attributes(
                key("type").value(type),
                key("constraints").value(constraints),
                key("optional").value(optional)
            )
}

// helper 함수: 간단하게 DSL처럼 사용
fun bodyField(
    path: String,
    description: String,
    type: String,
    constraints: String,
    optional: Boolean = false
) = DocField(path, description, type, constraints, optional).toFieldDescriptor()

// --------- Header Fields DSL ---------
data class HeaderField(
    val name: String,
    val description: String,
    val type: String = "String",
    val constraints: String = "",
    val optional: Boolean = false
) {
    fun toHeaderDescriptor() =
        headerWithName(name)
            .description(description)
            .attributes(
                key("type").value(type),
                key("constraints").value(constraints),
                key("optional").value(optional)
            )
}

fun headerField(
    name: String,
    description: String,
    type: String = "String",
    constraints: String = "",
    optional: Boolean = false
) = HeaderField(name, description, type, constraints, optional).toHeaderDescriptor()

// --------- Path Parameters DSL ---------
data class PathParamField(
    val name: String,
    val description: String,
    val optional: Boolean = false
) {
    fun toParamDescriptor() =
        parameterWithName(name)
            .description(description)
            .attributes(
                key("optional").value(optional)
            )
}

fun pathParam(
    name: String,
    description: String,
    optional: Boolean = false
) = PathParamField(name, description, optional).toParamDescriptor()

// --------- Query Parameters DSL ---------
data class QueryParamField(
    val name: String,
    val description: String,
    val optional: Boolean = false
) {
    fun toParamDescriptor() =
        parameterWithName(name)
            .description(description)
            .attributes(
                key("optional").value(optional)
            )
}

fun queryParam(
    name: String,
    description: String,
    optional: Boolean = false
) = QueryParamField(name, description, optional).toParamDescriptor()