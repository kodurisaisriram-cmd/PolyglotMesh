package com.polyglotmesh.webflux.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing metadata for a supported language runtime.
 * Designed to provide the frontend (Monaco Editor) with language identifiers and editor configurations.
 */
public class LanguageInfoDto {

    private String id;
    private String name;
    private List<String> aliases = new ArrayList<>();
    private String engineLanguage;
    private String monacoLanguage;
    private String version;

    public LanguageInfoDto() {
    }

    public LanguageInfoDto(String id, String name, List<String> aliases, String engineLanguage, String monacoLanguage, String version) {
        this.id = id;
        this.name = name;
        this.aliases = aliases != null ? aliases : new ArrayList<>();
        this.engineLanguage = engineLanguage;
        this.monacoLanguage = monacoLanguage;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getEngineLanguage() {
        return engineLanguage;
    }

    public void setEngineLanguage(String engineLanguage) {
        this.engineLanguage = engineLanguage;
    }

    public String getMonacoLanguage() {
        return monacoLanguage;
    }

    public void setMonacoLanguage(String monacoLanguage) {
        this.monacoLanguage = monacoLanguage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "LanguageInfoDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", aliases=" + aliases +
                ", engineLanguage='" + engineLanguage + '\'' +
                ", monacoLanguage='" + monacoLanguage + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
