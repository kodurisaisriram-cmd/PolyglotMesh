package com.polyglotmesh.webflux.service;

import com.polyglotmesh.webflux.config.PolyglotEngineProperties;
import com.polyglotmesh.webflux.dto.LanguageInfoDto;
import com.polyglotmesh.webflux.dto.SupportedLanguagesResponse;
import com.polyglotmesh.webflux.exception.UnsupportedLanguageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageServiceTest {

    private LanguageService languageService;
    private PolyglotEngineProperties properties;

    @BeforeEach
    void setUp() {
        properties = new PolyglotEngineProperties();
        languageService = new LanguageService(properties);
    }

    @ParameterizedTest
    @ValueSource(strings = {"python", "py", "PYTHON", "Py", "  py  ", "  Python  "})
    @DisplayName("Normalize Python aliases to canonical 'python'")
    void testNormalizePython(String input) {
        assertEquals("python", languageService.normalize(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript", "js", "JAVASCRIPT", "Js", "  js  ", "  JavaScript  "})
    @DisplayName("Normalize JavaScript aliases to canonical 'javascript'")
    void testNormalizeJavaScript(String input) {
        assertEquals("javascript", languageService.normalize(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ruby", "java", "c++", "go", "rust", "php", "typescript"})
    @DisplayName("Reject unadvertised or unsupported languages")
    void testRejectUnsupportedLanguages(String unsupported) {
        assertThrows(UnsupportedLanguageException.class, () -> languageService.normalize(unsupported));
        assertFalse(languageService.isSupported(unsupported));
    }

    @Test
    @DisplayName("Reject null or blank language input")
    void testRejectNullOrBlank() {
        assertThrows(UnsupportedLanguageException.class, () -> languageService.normalize(null));
        assertThrows(UnsupportedLanguageException.class, () -> languageService.normalize("   "));
        assertFalse(languageService.isSupported(null));
        assertFalse(languageService.isSupported("   "));
    }

    @Test
    @DisplayName("Map Python aliases to engine ID 'python'")
    void testToEngineLanguageIdPython() {
        assertEquals("python", languageService.toEngineLanguageId("python"));
        assertEquals("python", languageService.toEngineLanguageId("py"));
        assertEquals("python", languageService.toEngineLanguageId("PY"));
    }

    @Test
    @DisplayName("Map JavaScript aliases to engine ID 'js'")
    void testToEngineLanguageIdJavaScript() {
        assertEquals("js", languageService.toEngineLanguageId("javascript"));
        assertEquals("js", languageService.toEngineLanguageId("js"));
        assertEquals("js", languageService.toEngineLanguageId("JS"));
    }

    @Test
    @DisplayName("Retrieve supported languages list containing Python and JavaScript only")
    void testGetSupportedLanguages() {
        List<LanguageInfoDto> languages = languageService.getSupportedLanguages();
        assertNotNull(languages);
        assertEquals(2, languages.size());

        LanguageInfoDto python = languages.get(0);
        assertEquals("python", python.getId());
        assertEquals("Python", python.getName());
        assertTrue(python.getAliases().contains("py"));
        assertEquals("python", python.getEngineLanguage());

        LanguageInfoDto js = languages.get(1);
        assertEquals("javascript", js.getId());
        assertEquals("JavaScript", js.getName());
        assertTrue(js.getAliases().contains("js"));
        assertEquals("js", js.getEngineLanguage());
    }

    @Test
    @DisplayName("Build SupportedLanguagesResponse with configurable limits")
    void testGetSupportedLanguagesResponse() {
        SupportedLanguagesResponse response = languageService.getSupportedLanguagesResponse();
        assertNotNull(response);
        assertEquals(2, response.getLanguages().size());
        assertEquals(10000L, response.getDefaultTimeoutMs());
        assertEquals(100L, response.getMinTimeoutMs());
        assertEquals(30000L, response.getMaxTimeoutMs());
        assertEquals(20000, response.getMaxCodeLength());
    }

    @Test
    @DisplayName("Retrieve LanguageInfoDto by alias or ID")
    void testGetLanguageInfo() {
        Optional<LanguageInfoDto> infoPy = languageService.getLanguageInfo("py");
        assertTrue(infoPy.isPresent());
        assertEquals("python", infoPy.get().getId());

        Optional<LanguageInfoDto> infoJs = languageService.getLanguageInfo("js");
        assertTrue(infoJs.isPresent());
        assertEquals("javascript", infoJs.get().getId());

        Optional<LanguageInfoDto> infoRuby = languageService.getLanguageInfo("ruby");
        assertFalse(infoRuby.isPresent());
    }
}
