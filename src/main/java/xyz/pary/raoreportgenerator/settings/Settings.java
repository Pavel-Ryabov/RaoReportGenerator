package xyz.pary.raoreportgenerator.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Settings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(Settings.class);

    public static final String APP_DIR = System.getProperty("user.home") + File.separator
            + "documents" + File.separator + "RaoReportGenerator" + File.separator;
    private static final String SETTINGS_PATH = APP_DIR + "settings.rrgs";
    private static final String EXCLUSIONS_PATH = APP_DIR + "playreports-exclusions.txt";

    private static final String DEFAULT_OUTPUT_DIR = "";
    private static final String DEFAULT_MOVIES_INFO_FILE = "movies-info";
    private static final String DEFAULT_PLAYREPORTS_FILE = "play-reports";
    private static final String DEFAULT_RAO_FILE = "rao";

    private static final String DATE = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss_").format(new Date());

    private static final String EXCEL_EXT = ".xlsx";

    private String inputDir;
    private String outputDir;
    private String moviesInfoFile;
    private String playReportsFile;
    private String raoFile;
    private transient List<String> playReportsExclusions;
    private StpSettings stpSettings;
    private ServerSettings serverSettings;

    public Settings() {
    }

    public Settings(String inputDir,
            String outputDir,
            String moviesInfoFile,
            String playReportsFile,
            String raoFile,
            List<String> playReportsExclusions,
            StpSettings stpSettings) {
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.moviesInfoFile = moviesInfoFile;
        this.playReportsFile = playReportsFile;
        this.raoFile = raoFile;
        this.playReportsExclusions = playReportsExclusions;
        this.stpSettings = stpSettings;
    }

    /**
     *
     * @return .xlsx
     */
    public String getExcelExt() {
        return EXCEL_EXT;
    }

    public String getInputDir() {
        if (inputDir != null) {
            return inputDir;
        }
        return getJarDirectory();
    }

    public void setInputDir(String inputDir) {
        if (!inputDir.endsWith(File.separator)) {
            inputDir = inputDir + File.separator;
        }
        this.inputDir = inputDir;
    }

    public boolean canWriteToInputDir() {
        return canWrite(getInputDir());
    }

    public String getOutputDir() {
        if (outputDir != null) {
            return outputDir;
        }
        return getJarDirectory();
    }

    public void setOutputDir(String outputDir) {
        if (!outputDir.endsWith(File.separator)) {
            outputDir = outputDir + File.separator;
        }
        this.outputDir = outputDir;
    }

    public boolean canWriteToOutputDir() {
        return canWrite(getOutputDir());
    }

    public String getMoviesInfoFile() {
        if (moviesInfoFile != null) {
            return moviesInfoFile;
        }
        return DEFAULT_MOVIES_INFO_FILE;
    }

    public String getMoviesInfoPath() {
        return getOutputDir() + DATE + getMoviesInfoFile() + EXCEL_EXT;
    }

    public void setMoviesInfoFile(String moviesInfoFile) {
        this.moviesInfoFile = moviesInfoFile;
    }

    public String getPlayReportsFile() {
        if (playReportsFile != null) {
            return playReportsFile;
        }
        return DEFAULT_PLAYREPORTS_FILE;
    }

    public String getPlayReportsPath() {
        return getOutputDir() + DATE + getPlayReportsFile() + EXCEL_EXT;
    }

    public void setPlayReportsFile(String playReportsFile) {
        this.playReportsFile = playReportsFile;
    }

    public String getRaoFile() {
        if (raoFile != null) {
            return raoFile;
        }
        return DEFAULT_RAO_FILE;
    }

    public String getRaoPath() {
        return getOutputDir() + DATE + getRaoFile() + EXCEL_EXT;
    }

    public void setRaoFile(String raoFile) {
        this.raoFile = raoFile;
    }

    public List<String> getPlayReportsExclusions() {
        if (playReportsExclusions != null) {
            return playReportsExclusions;
        }
        return Collections.emptyList();
    }

    public void setPlayReportsExclusions(List<String> playReportsExclusions) {
        this.playReportsExclusions = playReportsExclusions;
    }

    public StpSettings getStpSettings() {
        if (stpSettings != null) {
            return stpSettings;
        }
        return new StpSettings();
    }

    public void setStpSettings(StpSettings stpSettings) {
        this.stpSettings = stpSettings;
    }

    public ServerSettings getServerSettings() {
        if (serverSettings != null) {
            return serverSettings;
        }
        return new ServerSettings();
    }

    public void setServerSettings(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    public void save() {
        File outputDir = new File(getOutputDir());
        if (outputDir.isDirectory() && !outputDir.exists()) {
            outputDir.mkdirs();
        }
        File f = new File(SETTINGS_PATH);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        } catch (Exception e) {
            LOG.warn("Save settings exception: ", e);
        }
        PlayReportsExclusions.write(playReportsExclusions, EXCLUSIONS_PATH);
    }

    private void loadPlayReportsExclusions() {
        this.playReportsExclusions = PlayReportsExclusions.read(EXCLUSIONS_PATH);
    }

    private boolean canWrite(String path) {
        return Files.isWritable(new File(path).toPath());
    }

    public static Settings load() {
        File f = new File(SETTINGS_PATH);
        Settings settings = null;
        if (f.exists()) {
            try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(SETTINGS_PATH))) {
                settings = (Settings) oos.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOG.warn("Load settings exception: ", e);
            }
        }
        if (settings == null) {
            settings = new Settings();
        }
        settings.loadPlayReportsExclusions();
        return settings;
    }

    private static String getJarDirectory() {
        try {
            String path = new File(Settings.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getParentFile().getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path = path + File.separator;
            }
            return path;
        } catch (URISyntaxException e) {
            return DEFAULT_OUTPUT_DIR;
        }
    }
}
