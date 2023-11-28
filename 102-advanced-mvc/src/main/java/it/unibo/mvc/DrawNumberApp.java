package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
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

    private void config(){
        List<String> lines = new ArrayList<>();
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("config.yml");
        try (final BufferedReader buffReader = new BufferedReader(new InputStreamReader(inputStream))) {
            for(int i=0 ; i < 2 ; i++){
                lines.add(buffReader.readLine());
            }
            final List<Integer> splittedString = new ArrayList<>();
            lines.forEach(o -> splittedString.add(Integer.parseInt(o.split(": ").toString())));
            MIN=splittedString.get(0);
            MAX=splittedString.get(1);
            ATTEMPTS=splittedString.get(2);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        config();
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
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
