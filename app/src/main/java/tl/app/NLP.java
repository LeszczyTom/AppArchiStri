package tl.app;

import java.util.*;
import java.text.Normalizer;

class NLP {

    private final String[] prepositions = {"A", "dans", "par", "pour", "vers", "avec", "de", "sans", "sous"};
    private final String[] determiantns = {"du", "de", "un", "une", "le", "la", "l", "les", "des"};

    /**
     * https://stackoverflow.com/a/15190787
     */
    public String stripAccents(String s)
    {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public String[] removeUnwantedWords(String[] tokens) {
        LinkedList<String> tmp = new LinkedList<>(Arrays.asList(tokens));
        tmp.removeAll(Arrays.asList(determiantns));
        tmp.removeAll(Arrays.asList(prepositions));
        return tmp.toArray(new String[0]);
    }

    public Result coupleActionObjetDepuisPhrase(String phraseEntree) {
        Result res = new Result();
        // - Caractères spéciaux, majuscules -> minuscules puis tokenisation
        String[] tokens = stripAccents(phraseEntree).replaceAll("[^a-zA-Z0-9]+", " ").toLowerCase().split(" ");
        tokens = removeUnwantedWords(tokens);
        String action = getAction(tokens);
        if(action != null) {
            res.addAction(action);
            res.addObjet(getObject(tokens, res.getAction()));
            return res;
        }
        res.addAction(getOtherAction(tokens));
        return res;
    }

    public String getAction(String[] tokens) {
        if ("joue".equals(tokens[0])) {
            return "play";
        } else if ("pause".equals(tokens[0])) {
            return "pause";
        } else if ("baisse".equals(tokens[0])) {
            return "volumedown";
        } else if ("reprend".equals(tokens[0])) {
            return "resume";
        } else if ("monte".equals(tokens[0])) {
            return "volumeup";
        } else if ("passe".equals(tokens[0]) || "suivant".equals(tokens[0])) {
            return "forward";
        } else if ("revient".equals(tokens[0]) || "precedent".equals(tokens[0])) {
            return "backward";
        } else if ("stop".equals(tokens[0]) || "arrete".equals(tokens[0])) {
            return "stop";
        } else {
            return null;
        }
    }

    public String getOtherAction(String[] tokens) {
        if(tokens[0].equals("met") && tokens[1].equals("en") && tokens[2].equals("boucle")) return "shuffle";
        String prec = tokens[0];
        for(String token : tokens) {
            if(prec.equals("aime")) {
                if (token.equals("pas")) return "dislike";
                return "like";
            }
            prec = token;
        }
        if (prec.equals("aime")) return "like";
        return null;
    }

    public String getObject(String[] tokens, String action) {
        if(action == null || !action.equals("play")) return null;
        List<String> tmp = new ArrayList<>(Arrays.asList(tokens));
        tmp.remove(0);

        StringBuilder result = new StringBuilder();
        for(String token : tmp) {
            result.append(token);
            result.append(" ");
        }
        if(result.length() != 0 && result.charAt(result.length() - 1) == 32) result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}