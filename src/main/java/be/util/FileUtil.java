package be.util;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import org.springframework.stereotype.*;

@Component
@SuppressWarnings("UnusedReturnValue")
public class FileUtil {

    public boolean exists(Path path, LinkOption... options) {
        return Files.exists(path, options);
    }

    public Path createDirectories(Path dir, FileAttribute<?>... attrs)
            throws IOException {
        return Files.createDirectories(dir, attrs);
    }

    public byte[] readAllBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    public Path write(Path path, byte[] bytes, OpenOption... options) throws IOException {
        return Files.write(path, bytes, options);
    }

    public void delete(Path path) throws IOException {
        Files.delete(path);
    }
}
