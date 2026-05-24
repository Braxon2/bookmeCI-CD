package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.config.JwtService;
import com.dusanbranovic.bookme.controllers.PropertyController;
import com.dusanbranovic.bookme.dto.requests.BookableUnitRequestDTO;
import com.dusanbranovic.bookme.dto.requests.PropertyRequestDTO;
import com.dusanbranovic.bookme.dto.responses.PropertyDTO;
import com.dusanbranovic.bookme.service.PropertyService;
import com.dusanbranovic.bookme.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc
class PropertyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PropertyService propertyService;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private PropertyDTO mockPropertyDTO;

    @BeforeEach
    void setUp() {
        mockPropertyDTO = new PropertyDTO(
                1L, null, "Test Villa", null , "Serbia",
                "Belgrade", "Belgrade", "Test Address 123",
                "No smoking", List.of()
        );
    }

    @Test
    @WithMockUser
    void getAllProperties_ShouldReturn200AndList() throws Exception {
        Mockito.when(propertyService.getAll()).thenReturn(List.of(mockPropertyDTO));

        mockMvc.perform(get("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Villa"))
                .andExpect(jsonPath("$[0].city").value("Belgrade"));
    }

    @Test
    @WithMockUser(username = "owner@test.com")
    void addProperty_ShouldReturn200AndSavedProperty() throws Exception {
        PropertyRequestDTO requestDTO = new PropertyRequestDTO(
                null, "New Villa", "Desc", "Serbia", "Novi Sad",
                "Address", "Rules", "Info", List.of()
        );
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("owner@test.com");
        Mockito.when(propertyService.addProperty(any(PropertyRequestDTO.class), eq("owner@test.com")))
                .thenReturn(mockPropertyDTO);


        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Villa"));
    }

    @Test
    @WithMockUser
    void getProperty_ShouldReturn200AndProperty() throws Exception {
        Mockito.when(propertyService.getProperty(1L)).thenReturn(mockPropertyDTO);

        mockMvc.perform(get("/api/properties/{pid}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Villa"));
    }

    @Test
    @WithMockUser
    void uploadPropertyImage_ShouldReturn200AndImageUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes()
        );

        String mockUrl = "https://mock-s3-bucket.s3.amazonaws.com/test.jpg";
        Mockito.when(s3Service.uploadPropertyImage(eq(1L), any())).thenReturn(mockUrl);

        mockMvc.perform(multipart("/api/properties/{pid}/images", 1L)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(mockUrl));
    }

    @Test
    @WithMockUser
    void getThumbnail_ShouldReturn200AndUrlMap() throws Exception {
        String mockUrl = "https://mock-s3-bucket.s3.amazonaws.com/thumbnail.jpg";
        Mockito.when(propertyService.getThumbnail(1L)).thenReturn(mockUrl);

        mockMvc.perform(get("/api/properties/{pid}/thumbnail", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(mockUrl));
    }
}
