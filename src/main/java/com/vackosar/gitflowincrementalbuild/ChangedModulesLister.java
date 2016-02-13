package com.vackosar.gitflowincrementalbuild;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ChangedModulesLister {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private DiffLister diffLister;
    @Inject private ModuleDirLister moduleDirLister;

    public Set<Path> act(Path pom) throws GitAPIException, IOException {
        return diffLister.act().stream()
                .map(path -> findModulePath(path, pom))
                .filter(modulePath -> modulePath != null)
                .map(nonNullModulePath -> pom.getParent().relativize(nonNullModulePath))
                .collect(Collectors.toSet());
    }

    private Path findModulePath(Path diffPath, Path pom) {
        final List<Path> moduleDirs = moduleDirLister.act(pom);
        Path path = diffPath;
        while (path != null && ! moduleDirs.contains(path)) {
            path = path.getParent();
        }
        if (path == null) {
            logger.warn("File changed outside build project: " + diffPath);
        }
        return path;
    }

}
