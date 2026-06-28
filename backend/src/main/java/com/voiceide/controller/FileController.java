package com.voiceide.controller;

import com.voiceide.dto.WorkspaceFileInput;
import com.voiceide.dto.WorkspaceFileUpdate;
import com.voiceide.model.WorkspaceFile;
import com.voiceide.service.FileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping
    public ResponseEntity<List<WorkspaceFile>> listFiles(
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(fileService.listFiles(sessionId));
    }

    @PostMapping
    public ResponseEntity<WorkspaceFile> createFile(@Valid @RequestBody WorkspaceFileInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.createFile(input));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceFile> getFile(@PathVariable String id) {
        return fileService.getFile(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceFile> updateFile(
            @PathVariable String id,
            @Valid @RequestBody WorkspaceFileUpdate update) {
        return fileService.updateFile(id, update)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
