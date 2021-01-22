package org.bonitasoft.web.designer.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller that serve the language pack from UID workspace.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class I18Resource {

    private final WorkspacePathResolver workspacePathResolver;

    /**
     * Sample request: GET /i18n/lang-template-es-ES.json
     * @param languagePackName
     * @return An Angular get-text compliant json data.
     */
    @GetMapping(value = "/i18n/{languagePackName}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> languagePack(@PathVariable String languagePackName) throws IOException {
        final Path languagePackPath = workspacePathResolver.getTmpI18nRepositoryPath().resolve(languagePackName);
        if (!Files.exists(languagePackPath) && languagePackName.endsWith(".json")) {
            log.error("LanguagePack file {} not found in {}", languagePackName, languagePackPath);
            throw new NotFoundException("LanguagePack file not found: " + languagePackName);
        }
        final String content = Files.readString(languagePackPath, StandardCharsets.UTF_8);
        return ResponseEntity.ok(content);
    }

}
