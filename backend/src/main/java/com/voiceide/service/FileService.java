package com.voiceide.service;

import com.voiceide.dto.WorkspaceFileInput;
import com.voiceide.dto.WorkspaceFileUpdate;
import com.voiceide.model.WorkspaceFile;
import com.voiceide.repository.WorkspaceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private WorkspaceFileRepository fileRepository;

    public List<WorkspaceFile> listFiles(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return fileRepository.findBySessionId(sessionId);
        }
        return fileRepository.findBySessionIdIsNull();
    }

    public WorkspaceFile createFile(WorkspaceFileInput input) {
        WorkspaceFile file = new WorkspaceFile();
        file.setId(UUID.randomUUID().toString());
        file.setName(input.getName());
        file.setPath(input.getPath());
        file.setContent(input.getContent() != null ? input.getContent() : "");
        file.setLanguage(inferLanguage(input.getName(), input.getLanguage()));
        file.setSessionId(input.getSessionId());
        return fileRepository.save(file);
    }

    public Optional<WorkspaceFile> getFile(String id) {
        return fileRepository.findById(id);
    }

    public Optional<WorkspaceFile> updateFile(String id, WorkspaceFileUpdate update) {
        return fileRepository.findById(id).map(file -> {
            file.setContent(update.getContent());
            if (update.getName() != null) file.setName(update.getName());
            return fileRepository.save(file);
        });
    }

    public void deleteFile(String id) {
        fileRepository.deleteById(id);
    }

    private String inferLanguage(String filename, String explicitLanguage) {
        if (explicitLanguage != null && !explicitLanguage.isBlank()) return explicitLanguage;
        if (filename == null) return "text";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".py")) return "python";
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".cpp") || lower.endsWith(".cc")) return "cpp";
        if (lower.endsWith(".c")) return "c";
        return "text";
    }
}
