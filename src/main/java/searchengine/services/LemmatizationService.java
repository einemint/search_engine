package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LemmatizationService {
    private static LuceneMorphology luceneMorphRus;
    private static LuceneMorphology luceneMorphEng;

    private Pattern rusPattern;
    private Pattern engPattern;

    public void init() {
        try {
            this.luceneMorphRus = new RussianLuceneMorphology();
            this.luceneMorphEng = new EnglishLuceneMorphology();
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        this.rusPattern = Pattern.compile("[а-яё]+");
        this.engPattern = Pattern.compile("[a-z]+");
    }

    public Map<String, Integer> getLemmasWithFrequency(String text) {
        text = text.trim().toLowerCase();

        Map<String, Integer> lemmasWithFrequency = new HashMap<>();
        Set<String> lemmas = new HashSet<>();
        List<String> wordBaseForms;
        String[] textFragments = text.split(" ");

        for (String fragment: textFragments) {
            if (!isCorrectWord(fragment)) {
                continue;
            }

            if (isEng(fragment)) { wordBaseForms = luceneMorphEng.getNormalForms(fragment); }
            else { wordBaseForms = luceneMorphRus.getNormalForms(fragment); }
            String lemma = wordBaseForms.get(0);

            if (lemmasWithFrequency.containsKey(lemma)) {
                lemmasWithFrequency.replace(lemma, lemmasWithFrequency.get(lemma) + 1);
            } else {
                lemmasWithFrequency.put(lemma, 1);
            }
        }

        return lemmasWithFrequency;
    }

    public List<String> getLemmaForms(String lemma) {
        List<String> lemmaForms = new ArrayList<>();

        if (isCorrectWord(lemma)) {
            if (isRus(lemma)) {
                lemmaForms = (ArrayList<String>) luceneMorphRus.getMorphInfo(lemma);
            }
            else {
                lemmaForms = (ArrayList<String>) luceneMorphEng.getMorphInfo(lemma);
            }
        }

        return lemmaForms;
    }

    private boolean isCorrectWord(String word) {
        List<String> wordInfo;

        if (isEng(word)) {
            wordInfo = luceneMorphEng.getMorphInfo(word);
            for (String info : luceneMorphEng.getMorphInfo(word)) {
                if (!(info.contains("ARTICLE") || info.contains("CONJ") || info.contains("PREP")
                        || info.contains("INT"))) {
                    return true;
                }
            }
        }
        else if (isRus(word)) {
            wordInfo = luceneMorphRus.getMorphInfo(word);
            for (String info : wordInfo) {
                if (!(info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД")
                        || info.contains("ЧАСТ"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isRus(String lemma) {
        Matcher rusMatcher = rusPattern.matcher(lemma);
        if (rusMatcher.matches()) return true;
        else return false;
    }

    private boolean isEng(String lemma) {
        Matcher engMatcher = engPattern.matcher(lemma);
        if (engMatcher.matches()) return true;
        else return false;
    }
}
