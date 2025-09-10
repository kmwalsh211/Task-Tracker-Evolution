package edu.trincoll.tracker;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * AI Collaboration Report:
 * - AI Tool Used: ChatGPT
 * - Most Helpful Prompt: "how to do basic git commands"
 * - AI Mistake We Fixed: Nothing
 * - Time Saved: like 15 minutes
 * - Team Members: Kayla, Aj, Allan
 */
@RestController
@RequestMapping(value = "/api/Movies", produces = MediaType.APPLICATION_JSON_VALUE) // TODO: ReTitle to match your domain (e.g., /api/bookmarks, /api/recipes)
public class MovieController {

    // Simple in-memory store (will be replaced by a database later)
    private static final Map<Long, Movie> STORE = new ConcurrentHashMap<>();
    private static final AtomicLong ID_SEQ = new AtomicLong(1);

    /**
     * GET /api/Movies
     * Returns all Movies in the system
     */
    @GetMapping
    public ResponseEntity<List<Movie>> getAll() {
        List<Movie> Movies = STORE.values()
                .stream()
                .sorted(Comparator.comparing(Movie::getId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Movies);
    }

    /**
     * GET /api/Movies/{id}
     * Returns a specific Movie by ID
     * Return 404 if Movie doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getById(@PathVariable Long id) {
        Movie movie = STORE.get(id);
        if (movie == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(movie);
    }

    /**
     * POST /api/Movies
     * Creates a new Movie
     * - Validate required fields (Title)
     * - Reject duplicates by Title (409 Conflict)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> create(@RequestBody Movie movie) {
        System.out.println(movie);
        // Validate Title
        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // Enforce uniqueness by Title
        boolean duplicate = STORE.values().stream()
                .anyMatch(existing -> Objects.equals(existing.getTitle(), movie.getTitle()));
        if (duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Assign new ID (ignore any provided id)
        long id = ID_SEQ.getAndIncrement();
        Movie toSave = new Movie();
        toSave.setId(id);
        toSave.setTitle(movie.getTitle());
        toSave.setDescription(movie.getDescription());
        toSave.setWatched(movie.isWatched());
        // Keep server-controlled createdAt from constructor; do not override from client

        STORE.put(id, toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSave);
    }

    /**
     * PUT /api/Movies/{id}
     * Updates an existing Movie
     * - Validate required fields (Title)
     * - Return 404 if Movie doesn't exist
     * - Reject duplicates by Title (409 Conflict) if changing to an existing Title
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> update(@PathVariable Long id, @RequestBody Movie update) {
        Movie existing = STORE.get(id);
        System.out.println(update);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (update.getTitle() == null || update.getTitle().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // Prevent changing to a Title that duplicates another Movie's Title
        boolean duplicateTitle = STORE.values().stream()
                .anyMatch(other -> !Objects.equals(other.getId(), id)
                        && Objects.equals(other.getTitle(), update.getTitle()));
        if (duplicateTitle) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        existing.setTitle(update.getTitle());
        existing.setDescription(update.getDescription());
        existing.setWatched(update.isWatched());
        // Keep original createdAt (ignore client-sent value)

        return ResponseEntity.ok(existing);
    }

    /**
     * DELETE /api/Movies/{id}
     * Deletes a Movie
     * - Return 204 No Content on successful delete
     * - Return 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Movie removed = STORE.remove(id);
        if (removed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/Movies/search?Title=value
     * Searches Movies by Title (case-insensitive contains)
     * BONUS endpoint
     */
    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchByTitle(@RequestParam("Title") String Title) {
        if (Title == null) {
            return ResponseEntity.badRequest().build();
        }
        String query = Title.toLowerCase(Locale.ROOT);
        List<Movie> results = STORE.values().stream()
                .filter(it -> it.getTitle() != null && it.getTitle().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(Movie::getId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // Test helper method - only for testing purposes
    static void clearStore() {
        STORE.clear();
        ID_SEQ.set(1);
    }
}