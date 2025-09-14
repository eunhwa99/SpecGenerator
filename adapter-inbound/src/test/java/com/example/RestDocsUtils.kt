package com.example

import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.snippet.Attributes.key

// --------- Body Fields DSL ---------
data class DocField(
    val path: String,
    val description: String,
    val type: String = "String",
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

// helper 함수
fun bodyField(
    path: String,
    description: String,
    type: String = "String",
    constraints: String,
    optional: Boolean = false
) = DocField(path, description, type, constraints, optional).toFieldDescriptor()

// --------- Header Fields ---------
data class HeaderField(
    val path: String,
    val description: String,
    val type: String = "String",
    val constraints: String = "",
    val optional: Boolean = false
) {
    fun toHeaderDescriptor(): HeaderDescriptor =
        headerWithName(path)
            .description(description)
            .attributes(
                key("type").value(type),
                key("constraints").value(constraints),
                key("optional").value(optional)
            )
}

fun headerField(
    path: String,
    description: String,
    type: String = "String",
    constraints: String = "",
    optional: Boolean = false
) = HeaderField(path, description, type, constraints, optional).toHeaderDescriptor()

// --------- Path Parameters  ---------
data class PathParamField(
    val path: String,
    val description: String,
    val type: String = "String",
    val constraints: String = "",
    val optional: Boolean = true
) {
    fun toParamDescriptor(): ParameterDescriptor =
        parameterWithName(path)
            .description(description)
            .attributes(
                key("type").value(type),
                key("constraints").value(constraints),
                key("optional").value(optional)
            )
}

fun pathParam(
    path: String,
    description: String,
    type: String = "String",
    constraints: String = "",
    optional: Boolean = true
) = PathParamField(path, description, type, constraints, optional).toParamDescriptor()

// --------- Query Parameters  ---------
data class QueryParamField(
    val path: String,
    val description: String,
    val type: String = "String",
    val constraints: String = "",
    val optional: Boolean = true
) {
    fun toParamDescriptor(): ParameterDescriptor =
        parameterWithName(path)
            .description(description)
            .attributes(
                key("type").value(type),
                key("constraints").value(constraints),
                key("optional").value(optional)
            )
}

fun queryParam(
    path: String,
    description: String,
    type: String = "String",
    constraints: String = "",
    optional: Boolean = false
) = QueryParamField(path, description, type, constraints, optional).toParamDescriptor()