package com.example

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

// DTOs
data class ItemRequest(@field:Size(min = 1, max = 10) val name: String, val price: Int)
data class ItemResponse(
    val id: Long,
    val name: String,
    val price: Int,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class ErrorResponse(
    val message: String,
    val errors: List<FieldError>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class FieldError(val field: String, val message: String)

@RestController
@RequestMapping("/api/items")
class ItemController {

    private val items = mutableListOf<ItemResponse>()
    private val idGenerator = AtomicLong(1)

    init {
        // 테스트용 샘플 데이터
        items.add(
            ItemResponse(
                idGenerator.getAndIncrement(),
                "item1",
                100,
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        )
        items.add(
            ItemResponse(
                idGenerator.getAndIncrement(),
                "item2",
                200,
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        )
    }

    @GetMapping
    fun getItems(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) minPrice: Int?,
        @RequestParam(required = false) maxPrice: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): List<ItemResponse> {
        return items.filter {
            (name == null || it.name.contains(name)) &&
                    (minPrice == null || it.price >= minPrice) &&
                    (maxPrice == null || it.price <= maxPrice)
        }.drop(page * size).take(size)
    }

    @GetMapping("/{id}")
    fun getItemById(@PathVariable id: Long): ResponseEntity<Any> {
        val item = items.find { it.id == id }
        return if (item != null) ResponseEntity.ok(item)
        else ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("Item not found"))
    }

    @PostMapping
    fun createItem(@Valid @RequestBody request: ItemRequest): ResponseEntity<Any> {
        val errors = mutableListOf<FieldError>()
        if (request.name.isBlank()) errors.add(FieldError("name", "Name must not be blank"))
        if (request.price <= 0) errors.add(FieldError("price", "Price must be greater than 0"))

        if (errors.isNotEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse("Validation failed", errors))
        }

        val now = LocalDateTime.now()
        val item =
            ItemResponse(idGenerator.getAndIncrement(), request.name, request.price, now, now)
        items.add(item)
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Location", "/api/items/${item.id}")
            .body(item)
    }

    @PutMapping("/{id}")
    fun updateItem(@PathVariable id: Long, @RequestBody request: ItemRequest): ResponseEntity<Any> {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse("Item not found"))
        }

        val current = items[index]
        val updated = current.copy(
            name = request.name,
            price = request.price,
            updatedAt = LocalDateTime.now()
        )
        items[index] = updated
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteItem(@PathVariable id: Long) {
        items.removeIf { it.id == id }
    }
}
