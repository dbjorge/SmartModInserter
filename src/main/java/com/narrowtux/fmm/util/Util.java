package com.narrowtux.fmm.util;

import com.google.common.io.LittleEndianDataInputStream;
import com.narrowtux.fmm.gui.Controller;
import com.narrowtux.fmm.io.tasks.TaskService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Util {
    private static HashMap<URL, LoadTask> preloadTasks = new HashMap<>();
    private static HashMap<URL, Node> loaded = new HashMap<>();
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static DecimalFormat[] decimalFormats = {
            new DecimalFormat("#"), // B
            new DecimalFormat("#.0"), // KiB
            new DecimalFormat("#.0"), // MiB
            new DecimalFormat("#.00"), // GiB
            new DecimalFormat("#.000"), // TiB
            new DecimalFormat("#.000"), // PiB
            new DecimalFormat("#.000"), // EiB
            new DecimalFormat("#.000"), // ZiB
            new DecimalFormat("#.000") // YiB
    };
    private static String[] binaryIECMultipliers = {
            "Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi", "Yi"
    };

    public static String formatBytes(long bytes) {
        int currentMultiplier = 0;
        long tmp = bytes;
        while (tmp >= 1024) {
            tmp /= 1024;
            currentMultiplier ++;
        }
        double res = (double) bytes / Math.pow(1024, currentMultiplier);

        return decimalFormats[currentMultiplier].format(res) + " " + (currentMultiplier > 0 ? binaryIECMultipliers[currentMultiplier - 1] : "") + "B";
    }

    public static String readString(LittleEndianDataInputStream objectInputStream) throws IOException {
        int size = objectInputStream.readInt();
        byte read[] = new byte[size];
        objectInputStream.read(read);
        return new String(read);
    }

    public static <N extends Node, C extends Controller> LoadResult<N, C>  loadFXML(URL resource, Supplier<C> controller) throws IOException {
        if (preloadTasks.containsKey(resource)) {
            try {
                LoadTask loadTask = preloadTasks.get(resource);
                if (loadTask.getControllerTask().getController() == null) {
                    loadTask.getControllerTask().setController(controller.get());
                }
                FXMLLoader loader = loadTask.get();
                return new LoadResult<>(loader.getRoot(), loader.getController());
            } catch (InterruptedException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        N ret = FXMLLoader.load(resource, null, new JavaFXBuilderFactory(), clazz -> controller.get());
        loaded.put(resource, ret);
        return new LoadResult<>(ret, controller.get());
    }

    public static class LoadResult<N extends Node, C extends Controller> {
        private N node;
        private C controller;

        public LoadResult(N node, C controller) {
            this.node = node;
            this.controller = controller;
        }

        public N getNode() {
            return node;
        }

        public C getController() {
            return controller;
        }
    }

    public static <N extends Node, C extends Controller> LoadResult<N, C> loadFXML(String resourceUrl, Supplier<C> controller) throws IOException {
        return loadFXML(Util.class.getResource(resourceUrl), controller);
    }

    public static void preloadFXML(URL resource) {
        LoadTask task = new LoadTask(resource);
        preloadTasks.put(resource, task);

        executorService.submit(task);
    }

    public static void preloadFXML(URL resource, Controller controller) {
        LoadTask task = new LoadTask(resource, controller);
        preloadTasks.put(resource, task);

        executorService.submit(task);
    }

    public static void preloadFXML(String resourceUrl, Controller controller) {
        preloadFXML(Util.class.getResource(resourceUrl), controller);
    }

    public static void preloadFXML(String resourceUrl) {
        preloadFXML(Util.class.getResource(resourceUrl));
    }

    public static void clearPreloadThreads() {
        executorService.shutdown();
        executorService = Executors.newCachedThreadPool();
    }

    private static class LoadTask extends Task<FXMLLoader> {

        private URL resource;
        private ControllerTask controllerTask = new ControllerTask();

        public LoadTask(URL resource) {
            this.resource = resource;
        }

        public LoadTask(URL resource, Controller controller) {
            this.resource = resource;
            controllerTask.setController(controller);
        }

        @Override
        protected FXMLLoader call() throws Exception {
            FXMLLoader loader = new FXMLLoader(resource);
            executorService.submit(controllerTask);
            loader.setControllerFactory((aClass -> {
                try {
                    Controller ret = controllerTask.get();
                    return ret;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                throw new IllegalStateException();
            }));
            loader.load();
            return loader;
        }

        public ControllerTask getControllerTask() {
            synchronized (controllerTask) {
                return controllerTask;
            }
        }
    }

    private static class ControllerTask extends Task<Controller> {

        private Controller controller = null;
        private Thread thread;

        @Override
        protected Controller call() throws Exception {
            thread = Thread.currentThread();
            while (controller == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            return controller;
        }

        public Controller getController() {
            return controller;
        }

        public void setController(Controller controller) {
            this.controller = controller;
            set(controller);
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        }
    }

    public static Path tryGetEnvPath(String environementVariableName) {
        String maybeEnvValue = System.getenv(environementVariableName);
        if (maybeEnvValue == null) return null;
        return Paths.get(maybeEnvValue);
    }
}
