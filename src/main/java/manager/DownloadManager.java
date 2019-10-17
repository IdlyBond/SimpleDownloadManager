package manager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class DownloadManager {

    public static void main(String[] args) {
        DownloadManager downloadManager = new DownloadManager();
        downloadManager.download(new File("file"), "test.txt");
        downloadManager.download(new File("file"), "testWithBuffer.txt", 1024);
        downloadManager.download(new File("file"), "testWithBuffer&Time.txt", 1024, 2);
    }

    public void download(File file, String dest) {
        try {
            check(file, dest);
            copy(file, Paths.get(dest).toFile());
        } catch (IOException e) {
            System.out.println("Не удалось скопировать файл: " + e.getMessage());
        }
    }

    public void download(File file, String dest, int bufferSize) {
        try {
            if (bufferSize <= 0)
                throw new IllegalArgumentException("Buffer should be > than 0. Your buffer is " + bufferSize);
            check(file, dest);
            copy(file, Paths.get(dest).toFile(), bufferSize);
        } catch (IOException e) {
            System.out.println("Не удалось скопировать файл: " + e.getMessage());
        }
    }

    public void download(File file, String dest, int bufferSize, int timeLimitMils) {
        try {
            check(file, dest);
            copy(file, Paths.get(dest).toFile(), bufferSize, timeLimitMils);
        } catch (IOException e) {
            System.out.println("Не удалось скопировать файл: " + e.getMessage());
        } catch (TimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    private void check(File file, String dest) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException("No file \'" + file.getName() + "\' to copy from");
        if (file.getUsableSpace() < file.length())
            throw new IOException("Not enough space to copy " + file.getName());
        Path destPath = Paths.get(dest);
        if (Objects.nonNull(destPath.getParent())) Files.createDirectories(destPath.getParent());
        if (Files.notExists(destPath)) {
            if (Files.isDirectory(destPath)) Files.createDirectory(destPath);
            else Files.createFile(destPath);
        }
    }

    private void copy(File from, File to) throws IOException {
        try (FileInputStream is = new FileInputStream(from);
             FileOutputStream os = new FileOutputStream(to)) {
            byte[] buffer = new byte[4096];
            int data;
            while ((data = is.read(buffer)) != -1) {
                os.write(buffer, 0, data);
            }
        }
    }

    private void copy(File from, File to, int bufferSize) throws IOException {
        try (FileInputStream is = new FileInputStream(from);
             FileOutputStream os = new FileOutputStream(to)) {
            byte[] buffer = new byte[bufferSize];
            int data;
            while ((data = is.read(buffer)) != -1) {
                os.write(buffer, 0, data);
            }
        }
    }

    private void copy(File from, File to, int bufferSize, int timeLimitMils) throws IOException, TimeoutException {
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(from));
             BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(to))) {
            byte[] buffer = new byte[bufferSize];
            int data;
            LocalTime startTime = LocalTime.now();
            while ((data = is.read(buffer)) != -1) {
                os.write(buffer, 0, data);
                if (Duration.between(startTime, LocalTime.now()).toMillis() > timeLimitMils)
                    throw new TimeoutException("Coping took longer than " + timeLimitMils);
            }
        }
    }
}
