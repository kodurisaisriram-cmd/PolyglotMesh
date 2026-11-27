package com.polyglotmesh.webflux.service;

import com.polyglotmesh.webflux.config.PolyglotEngineProperties;
import com.polyglotmesh.webflux.dto.LanguageInfoDto;
import com.polyglotmesh.webflux.dto.SupportedLanguagesResponse;
import com.polyglotmesh.webflux.exception.UnsupportedLanguageException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service managing supported guest language runtimes, aliases, and canonical language normalization.
 * Ensures the API boundary strictly permits only genuine GraalVM languages (Python and JavaScript)
 * and safely rejects unsupported languages such as Ruby or Java.
 */
@Service
public class LanguageService {

    public static final String CANONICAL_PYTHON = "python";
    public static final String CANONICAL_JAVASCRIPT = "javascript";

    public static final String ENGINE_ID_PYTHON = "python";
    public static final String ENGINE_ID_JAVASCRIPT = "js";

    private final PolyglotEngineProperties properties;
    private final Map<String, LanguageInfoDto> languageDefinitions = new LinkedHashMap<>();
    private final Map<String, String> aliasToCanonicalMap = new LinkedHashMap<>();
    private final Map<String, String> canonicalToEngineIdMap = new LinkedHashMap<>();

    public LanguageService(PolyglotEngineProperties properties) {
        this.properties = properties;
        initializeLanguages();
    }

    private void initializeLanguages() {
        // Python Definition
        LanguageInfoDto python = new LanguageInfoDto(
                CANONICAL_PYTHON,
                "Python",
                List.of("python", "py"),
                ENGINE_ID_PYTHON,
                "python",
                "3.11 (GraalPy)"
        );
        languageDefinitions.put(CANONICAL_PYTHON, python);
        aliasToCanonicalMap.put("python", CANONICAL_PYTHON);
        aliasToCanonicalMap.put("py", CANONICAL_PYTHON);
        canonicalToEngineIdMap.put(CANONICAL_PYTHON, ENGINE_ID_PYTHON);

        // JavaScript Definition
        LanguageInfoDto javascript = new LanguageInfoDto(
                CANONICAL_JAVASCRIPT,
                "JavaScript",
                List.of("javascript", "js"),
                ENGINE_ID_JAVASCRIPT,
                "javascript",
                "ES2023 (GraalJS)"
        );
        languageDefinitions.put(CANONICAL_JAVASCRIPT, javascript);
        aliasToCanonicalMap.put("javascript", CANONICAL_JAVASCRIPT);
        aliasToCanonicalMap.put("js", CANONICAL_JAVASCRIPT);
        canonicalToEngineIdMap.put(CANONICAL_JAVASCRIPT, ENGINE_ID_JAVASCRIPT);
    }

    /**
     * Normalizes a given language string or alias into its canonical representation.
     * E.g., "py" -> "python", "JS" -> "javascript".
     *
     * @param language raw input language identifier or alias
     * @return canonical language name ("python" or "javascript")
     * @throws UnsupportedLanguageException if the language is unsupported, null, or blank
     */
    public String normalize(String language) {
        if (language == null || language.trim().isEmpty()) {
            throw new UnsupportedLanguageException("Language cannot be null or blank");
        }
        String clean = language.trim().toLowerCase();
        String canonical = aliasToCanonicalMap.get(clean);
        if (canonical == null) {
            throw new UnsupportedLanguageException(language);
        }
        return canonical;
    }

    /**
     * Maps a canonical language identifier or alias to the underlying GraalVM engine ID.
     * E.g., "javascript" or "js" -> "js"; "python" or "py" -> "python".
     *
     * @param language canonical language or alias
     * @return GraalVM engine language identifier
     * @throws UnsupportedLanguageException if the language is unsupported
     */
    public String toEngineLanguageId(String language) {
        String canonical = normalize(language);
        return canonicalToEngineIdMap.get(canonical);
    }

    /**
     * Verifies whether the specified language or alias is supported.
     *
     * @param language language name or alias
     * @return true if supported, false otherwise
     */
    public boolean isSupported(String language) {
        if (language == null || language.trim().isEmpty()) {
            return false;
        }
        return aliasToCanonicalMap.containsKey(language.trim().toLowerCase());
    }

    /**
     * Returns metadata for all supported language runtimes.
     *
     * @return unmodifiable list of LanguageInfoDto
     */
    public List<LanguageInfoDto> getSupportedLanguages() {
        return Collections.unmodifiableList(List.copyOf(languageDefinitions.values()));
    }

    /**
     * Builds the full SupportedLanguagesResponse including runtime limits for frontend configuration.
     *
     * @return SupportedLanguagesResponse payload
     */
    public SupportedLanguagesResponse getSupportedLanguagesResponse() {
        return new SupportedLanguagesResponse(
                getSupportedLanguages(),
                properties.getDefaultTimeoutMs(),
                properties.getMinTimeoutMs(),
                properties.getMaxTimeoutMs(),
                properties.getMaxCodeLength()
        );
    }

    /**
     * Retrieves language metadata by its canonical ID or alias.
     *
     * @param language language identifier or alias
     * @return Optional containing LanguageInfoDto if supported
     */
    public Optional<LanguageInfoDto> getLanguageInfo(String language) {
        if (language == null) {
            return Optional.empty();
        }
        String canonical = aliasToCanonicalMap.get(language.trim().toLowerCase());
        return Optional.ofNullable(languageDefinitions.get(canonical));
    }
}
