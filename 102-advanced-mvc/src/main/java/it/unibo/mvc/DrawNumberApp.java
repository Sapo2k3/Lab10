package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static int MIN = 0;
    private static int MAX = 100;
    private static int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    private void config(final String configFile){
        List<String> lines = new ArrayList<>();
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(configFile);
        try (final BufferedReader buffReader = new BufferedReader(new InputStreamReader(inputStream))) {
            for(int i=0 ; i < 3 ; i++){
                try {
                    lines.add(buffReader.readLine());
                } catch ( IllegalArgumentException | UnsupportedOperationException e){
                    for(final DrawNumberView view : views){
                        view.displayError(e.getMessage());
                    }
                }      
            }
            lines.forEach(o -> {
                final String[] splittedString;
                splittedString = o.split(": ");
                if(splittedString.length == 2){
                    final int val = Integer.parseInt(splittedString[1]);
                    if(splittedString[0].contains("max")) {
                        MAX=val;
                    } else if(splittedString[0].contains("min")) {
                        MIN=val;
                    } else if(splittedString[0].contains("attempts")) {
                        ATTEMPTS=val;
                    }
                }
            });

        } catch (IOException | NumberFormatException e) {
           displayError(e.getMessage());
        }
    }

    private void displayError(final String message) {
        for (final DrawNumberView view: views) {
            view.displayError(message);
        }
    }

    /**
     * @param views
     *            the views to attach
     * @throws IOException
     */
    public DrawNumberApp(final String configFile, final DrawNumberView... views) throws IOException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            if(configFile.isBlank()){
                view.displayError(configFile);
                throw new java.io.IOException("Specify a file name");
            }
            view.setObserver(this);
            view.start();
        }
        config(configFile);
        this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
    }

    @Override  
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        try {
            new DrawNumberApp("config.yml", 
                    new DrawNumberViewImpl(),
                    new DrawNumberViewImpl(),
                    new PrintStreamView(System.out),
                    new PrintStreamView("output.log"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
