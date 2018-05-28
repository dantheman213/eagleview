package eagleview.tasks;

import eagleview.App;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class VideoDownloadTask extends Task<Void> {
    private TextArea textLog;
    private String url;

    public VideoDownloadTask(String url, TextArea textField) {
        this.url = url;
        this.textLog = textField;
    }

    @Override
    protected Void call() {
        BufferedReader bufferedReaderStdOut = null;
        BufferedReader bufferedReaderStdErr = null;

        try {
            System.out.println("Starting video download thread...");

            String[] cmdArgs = {
                    App.config.settings.youtubeDlBin,
                    "-o %(title)s.%(ext)s",
                    "-f worstvideo[ext=mp4]",
                    url
            };

            ProcessBuilder procBuilder = new ProcessBuilder(cmdArgs);
            procBuilder.directory(new File(App.config.settings.videoCollectionDir));
            Process proc = procBuilder.start();

            Thread threadStdOut = new Thread(new ProcessStreamReaderTask(proc.getInputStream(), this.textLog));
            threadStdOut.setDaemon(true);
            threadStdOut.start();

            Thread threadStdErr = new Thread(new ProcessStreamReaderTask(proc.getErrorStream(), this.textLog));
            threadStdErr.setDaemon(true);
            threadStdErr.start();

            // wait for both threads to finish before continuing
            threadStdOut.join();
            threadStdErr.join();

            proc.waitFor();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("Finished.. closing buffer.");
            if(bufferedReaderStdOut != null) {
                try {bufferedReaderStdOut.close();} catch(Exception ex) {}
            }
        }

        return null;
    }
}