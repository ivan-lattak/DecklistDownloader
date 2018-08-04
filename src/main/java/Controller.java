import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public class Controller {
    private Stage mainStage = null;

    @FXML
    private Label decklistURLLabel;
    @FXML
    private Label decklistTitleLabel;
    @FXML
    private Label outputFileLabel;

    @FXML
    private TextField decklistURL;
    @FXML
    private TextField decklistTitle;
    @FXML
    private TextField outputFile;

    @FXML
    private Button outputFileButton;
    @FXML
    private Button downloadButton;

    public void setOutputFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save decklist as...");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        outputFile.setText(Optional
                .ofNullable(chooser.showSaveDialog(mainStage))
                .map(f -> {
                    try {
                        return f.getCanonicalPath();
                    } catch (IOException ex) {
                        throw new UncheckedIOException("Failed to canonicalize file path", ex);
                    }
                })
                .orElse(""));
    }

    public void download() {
        disableInputs();
        Task<Void> download = new Task<>() {
            @Override
            protected Void call() {
                Downloader.download(decklistURL.getText(), decklistTitle.getText(), outputFile.getText());
                return null;
            }
        };
        download.setOnSucceeded(e -> enableInputs());
        download.setOnFailed(e -> enableInputs());
        new Thread(download).start();
    }

    public void quit() {
        mainStage.close();
    }

    void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    private void disableInputs() {
        for (Node node : getInputNodes()) {
            node.setDisable(true);
        }
    }

    private void enableInputs() {
        for (Node node : getInputNodes()) {
            node.setDisable(false);
        }
    }

    private Node[] getInputNodes() {
        return new Node[] {decklistURLLabel, decklistTitleLabel, outputFileLabel,
                decklistURL, decklistTitle, outputFile,
                outputFileButton, downloadButton};
    }
}
