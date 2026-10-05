package fairteam_backend.observer;

import java.util.ArrayList;
import java.util.List;

public class AcademicSubject {

    private final List<AcademicObserver> observers = new ArrayList<>();

    public void addObserver(AcademicObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AcademicObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(AcademicEvent event) {
        for (AcademicObserver observer : observers) {
            observer.update(event);
        }
    }
}