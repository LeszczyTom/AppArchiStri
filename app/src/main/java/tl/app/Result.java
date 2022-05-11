package tl.app;

public class Result {
    private String action;
    private String objet;

    public Result() {
        addAction(null);
        addObjet(null);
    }

    public Result(String action, String objet) {
        addAction(action);
        addObjet(objet);
    }

    public Result(String action) {
        addAction(action);
        addObjet(null);
    }

    public void addAction(String action) {
        this.action = action;
    }

    public void  addObjet(String objet) {
        this.objet = objet;
    }

    public String getAction() {
        return action;
    }

    public String getObjet() {
        return objet;
    }

    public String toString() {
        return String.format("<%1s, %2s>", action, objet);
    }
}