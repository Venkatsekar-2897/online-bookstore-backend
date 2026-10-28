package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDtos.*;
import com.example.bookstore.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Browse the book catalogue. Creating, updating, and deleting books requires ADMIN authority.")
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "List books", description = "Returns a paginated list of books. Defaults to 20 books per page, sorted by title. Maximum page size is 100.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Book page returned successfully")})
    public Page<BookResponse> all(@Parameter(description = "Pagination and sorting options; for example, page=0&size=20&sort=title") @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        log.debug("Listing books: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return bookService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book", description = "Returns the details of a book by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found", content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book does not exist", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public BookResponse one(@Parameter(description = "Unique book identifier", example = "1", in = ParameterIn.PATH, required = true) @PathVariable Long id) {
        log.debug("Fetching book id={}", id);
        return bookService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a book", description = "Adds a book to the catalogue. Requires a JWT with ADMIN scope.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created", content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid book details", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required")
    })
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        log.info("Creating book title='{}'", request.title());
        BookResponse created = bookService.create(request);
        log.info("Created book id={}", created.id());
        return created;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Update a book", description = "Replaces the editable details of an existing book. Requires a JWT with ADMIN scope.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated", content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid book details", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Book does not exist", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public BookResponse update(@Parameter(description = "Unique book identifier", example = "1", required = true) @PathVariable Long id, @Valid @RequestBody BookRequest request) {
        log.info("Updating book id={}", id);
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a book", description = "Deletes a book from the catalogue. Requires a JWT with ADMIN scope.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN authority required"),
            @ApiResponse(responseCode = "404", description = "Book does not exist", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public void delete(@Parameter(description = "Unique book identifier", example = "1", required = true) @PathVariable Long id) {
        log.info("Deleting book id={}", id);
        bookService.delete(id);
    }
}
