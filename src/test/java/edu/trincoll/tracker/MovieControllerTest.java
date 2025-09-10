package edu.trincoll.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for the Movie API.
 * <p>
 * ALL TESTS MUST PASS for full credit.
 * Do not modify these tests - modify your code to make them pass.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Movie Controller Tests")
class MovieControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clear any existing data before each test
        MovieController.clearStore();
    }
    
    @Nested
    @DisplayName("GET /api/Movies")
    class GetAllMovies {
        
        @Test
        @DisplayName("should return empty list when no Movies exist")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/Movies"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
        }
        
        @Test
        @DisplayName("should return all Movies when Movies exist")
        void shouldReturnAllMovies() throws Exception {
            // Create a test Movie first
            Movie testMovie = new Movie();
            testMovie.setTitle("Test Movie");
            testMovie.setDescription("Test Description");
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testMovie)))
                    .andExpect(status().isCreated());
            
            // Now get all Movies
            mockMvc.perform(get("/api/Movies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[?(@.Name == 'Test Movie')]").exists());
        }
    }
    
    @Nested
    @DisplayName("GET /api/Movies/{id}")
    class GetMovieById {
        
        @Test
        @DisplayName("should return Movie when it exists")
        void shouldReturnMovieWhenExists() throws Exception {
            // Create a test Movie first
            Movie testMovie = new Movie();
            testMovie.setTitle("Specific Movie");
            testMovie.setDescription("Specific Description");
            
            String response = mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testMovie)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Movie createdMovie = objectMapper.readValue(response, Movie.class);
            
            // Get the specific Movie
            mockMvc.perform(get("/api/Movies/{id}", createdMovie.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Title").value("Specific Movie"))
                    .andExpect(jsonPath("$.description").value("Specific Description"));
        }
        
        @Test
        @DisplayName("should return 404 when Movie doesn't exist")
        void shouldReturn404WhenMovieDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/Movies/{id}", 999999))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("POST /api/Movies")
    class CreateMovie {
        
        @Test
        @DisplayName("should create new Movie with valid data")
        void shouldCreateNewMovie() throws Exception {
            Movie newMovie = new Movie();
            newMovie.setTitle("New Movie");
            newMovie.setDescription("New Description");
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newMovie)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.Name").value("New Movie"))
                    .andExpect(jsonPath("$.description").value("New Description"));
        }
        
        @Test
        @DisplayName("should return 400 when Title is missing")
        void shouldReturn400WhenTitleMissing() throws Exception {
            String invalidJson = """
                    {"description":"No Title provided"}""";
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("should return 400 when Title is blank")
        void shouldReturn400WhenTitleBlank() throws Exception {
            Movie invalidMovie = new Movie();
            invalidMovie.setTitle("");  // Blank Title
            invalidMovie.setDescription("Valid Description");
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidMovie)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("should not allow duplicate Movies with same Title")
        void shouldNotAllowDuplicates() throws Exception {
            Movie firstMovie = new Movie();
            firstMovie.setTitle("Unique Title");
            firstMovie.setDescription("First Description");
            
            // Create first Movie
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstMovie)))
                    .andExpect(status().isCreated());
            
            // Try to create duplicate
            Movie duplicateMovie = new Movie();
            duplicateMovie.setTitle("Unique Title");  // Same Title
            duplicateMovie.setDescription("Different Description");
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateMovie)))
                    .andExpect(status().isConflict());  // 409 Conflict
        }
    }
    
    @Nested
    @DisplayName("PUT /api/Movies/{id}")
    class UpdateMovie {
        
        @Test
        @DisplayName("should update existing Movie")
        void shouldUpdateExistingMovie() throws Exception {
            // Create initial Movie
            Movie initialMovie = new Movie();
            initialMovie.setTitle("Original Title");
            initialMovie.setDescription("Original Description");
            
            String response = mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialMovie)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Movie createdMovie = objectMapper.readValue(response, Movie.class);
            
            // Update the Movie
            Movie updatedMovie = new Movie();
            updatedMovie.setTitle("Updated Title");
            updatedMovie.setDescription("Updated Description");
            updatedMovie.setWatched(true);
            
            mockMvc.perform(put("/api/Movies/{id}", createdMovie.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedMovie)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Title").value("Updated Title"))
                    .andExpect(jsonPath("$.description").value("Updated Description"))
                    .andExpect(jsonPath("$.completed").value(true));
        }
        
        @Test
        @DisplayName("should return 404 when updating non-existent Movie")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            Movie updateMovie = new Movie();
            updateMovie.setTitle("Update Title");
            updateMovie.setDescription("Update Description");
            
            mockMvc.perform(put("/api/Movies/{id}", 999999)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateMovie)))
                    .andExpect(status().isNotFound());
        }
        
        @Test
        @DisplayName("should validate required fields on update")
        void shouldValidateRequiredFieldsOnUpdate() throws Exception {
            // Create initial Movie
            Movie initialMovie = new Movie();
            initialMovie.setTitle("Original Title");
            initialMovie.setDescription("Original Description");
            
            String response = mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialMovie)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Movie createdMovie = objectMapper.readValue(response, Movie.class);
            
            // Try to update with invalid data
            String invalidUpdate = "{\"Title\":\"\",\"description\":\"Valid Description\"}";
            
            mockMvc.perform(put("/api/Movies/{id}", createdMovie.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidUpdate))
                    .andExpect(status().isBadRequest());
        }
    }
    
    @Nested
    @DisplayName("DELETE /api/Movies/{id}")
    class DeleteMovie {
        
        @Test
        @DisplayName("should delete existing Movie")
        void shouldDeleteExistingMovie() throws Exception {
            // Create Movie to delete
            Movie MovieToDelete = new Movie();
            MovieToDelete.setTitle("Delete Me");
            MovieToDelete.setDescription("To Be Deleted");
            
            String response = mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(MovieToDelete)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Movie createdMovie = objectMapper.readValue(response, Movie.class);
            
            // Delete the Movie
            mockMvc.perform(delete("/api/Movies/{id}", createdMovie.getId()))
                    .andExpect(status().isNoContent());
            
            // Verify it's gone
            mockMvc.perform(get("/api/Movies/{id}", createdMovie.getId()))
                    .andExpect(status().isNotFound());
        }
        
        @Test
        @DisplayName("should return 404 when deleting non-existent Movie")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            mockMvc.perform(delete("/api/Movies/{id}", 999999))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("Bonus: Search Functionality")
    class SearchMovies {
        
        @Test
        @DisplayName("BONUS: should search Movies by Title")
        void shouldSearchMoviesByTitle() throws Exception {
            // Create test Movies
            Movie Movie1 = new Movie();
            Movie1.setTitle("Apple");
            Movie1.setDescription("Red fruit");
            
            Movie Movie2 = new Movie();
            Movie2.setTitle("Banana");
            Movie2.setDescription("Yellow fruit");
            
            Movie Movie3 = new Movie();
            Movie3.setTitle("Application");
            Movie3.setDescription("Software");
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Movie1)))
                    .andExpect(status().isCreated());
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Movie2)))
                    .andExpect(status().isCreated());
            
            mockMvc.perform(post("/api/Movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Movie3)))
                    .andExpect(status().isCreated());
            
            // Search for Movies containing "App"
            mockMvc.perform(get("/api/Movies/search")
                    .param("Title", "App"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[?(@.Title == 'Apple')]").exists())
                    .andExpect(jsonPath("$[?(@.Title == 'Application')]").exists());
        }
    }
}