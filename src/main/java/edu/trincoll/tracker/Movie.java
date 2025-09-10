package edu.trincoll.tracker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base entity class for your domain object.
 * <p>
 * TODO: Rename this class to match your chosen domain
 * Examples: Bookmark, Quote, Habit, Recipe, Movie
 * <p>
 * TODO: Add at least 3 meaningful fields beyond 'id'
 * Examples for different domains:
 * - Bookmark: url, title, category, tags
 * - Quote: text, author, source, category
 * - Habit: name, frequency, streak, lastCompleted
 * - Recipe: name, ingredients, instructions, prepTime
 * - Movie: title, director, year, rating, watched
 */
public class Movie {
    
    private Long id;
    
    // TODO: Replace these example fields with your domain-specific fields
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private String director;
    private int year;
    private int rating;
    private boolean watched;
    
    // Constructor
    public Movie(String description, String title, String director, int year, int rating, boolean watched) {
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.title = title;
        this.director = director;
        this.year = year;
        this.rating = rating;
        this.watched = watched;
    }

    public Movie(){
        //description = "default";
        createdAt = LocalDateTime.now();
        //title = "default";
        director = "default";
        year = 0;
        rating = 0;
        watched = false;
    }
    
    // TODO: Generate getters and setters for all your fields
    // Hint: IntelliJ can do this for you (Alt+Insert or Cmd+N)
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public int getYear() {
        return year;
    }

    public int getRating() {
        return rating;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    // TODO: Consider overriding equals() and hashCode() based on your domain
    // This is important for testing and collections

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return watched == movie.watched &&
               Objects.equals(id, movie.id) &&
               Objects.equals(title, movie.title) &&
               Objects.equals(description, movie.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, watched);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", completed=" + watched +
                '}';
    }
}