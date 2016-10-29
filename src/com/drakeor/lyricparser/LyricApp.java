package com.drakeor.lyricparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class LyricApp {

    // Container for reserved words
    private HashSet<String> reservedWords;

    public LyricApp() {

        // Un-comment below to omit commonly used words like "and" and "the"
        reservedWords = new HashSet<>();
        String reservedList[] = {
                // Conjunctions
                //"for", "and", "nor", "but", "or", "yet", "so"

                // Definite / Indefinite articles
                //,"a", "an", "the"

                // Personal Pronouns
                //,"i", "me", "we", "us", "you", "he", "him", "her", "she", "it", "they", "them"

                // Interrogative pro-forms
                //,"who", "whom", "what", "where", "why", "when"
        };
        reservedWords.addAll(Arrays.asList(reservedList));
    }

    // Splits out words and adds them to the list
    private void FilterWords(String lyric, HashMap<String, Integer> listOfWords) {
        String[] words = lyric.split(" ");
        for(String word : words) {
            word = word.trim();
            if(!word.isEmpty() && !reservedWords.contains(word)) {
                Integer currentWordCount = listOfWords.get(word);
                if (currentWordCount == null) {
                    listOfWords.put(word, 1);
                } else {
                    listOfWords.put(word, currentWordCount + 1);
                }
            }
        }
    }

    // Grabs the top 100 songs from songlyrics.com
    public void Run() {

        // Few starting variables
        Document doc = null;
        HashMap<String, Integer> wordList = new HashMap<String, Integer>();
        LinkedHashSet<String> songList = new LinkedHashSet<String>();

        // Try to access the main document
        try {
            doc = Jsoup.connect("http://www.songlyrics.com/top100.php").get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Grab the table on this page that has a list of all the tracks
        Element content = doc.getElementsByClass("tracklist").get(0);

        // Only grab the last cell of each table to prevent duplicate links
        Elements correctRows = content.getElementsByClass("td-last");

        // Scrape the links and add them to the hashset
        for(Element correctRow : correctRows) {
            Element link = correctRow.getElementsByTag("a").get(0);
            songList.add(link.attr("href"));
        }

        // Iterate through our hashset
        int songNumber = 1;
        for(String song : songList) {
            System.out.println("Getting song " + songNumber + "/" + songList.size() + " : " +  song);
            String lyrics = GetSongLyrics(song);
            System.out.println(lyrics);
            FilterWords(lyrics, wordList);
            ++songNumber;
        }

        // Show all our words and write to a file
        try {
            PrintWriter writer = new PrintWriter("data.csv", "UTF-8");
            writer.println("Word,Count");
            for (HashMap.Entry<String, Integer> entry : wordList.entrySet()) {
                String word = entry.getKey();
                Integer wordCount = entry.getValue();
                writer.println(word + "," + wordCount);
                System.out.println(word + " : " + wordCount);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    // Get lyrics from a single song designated by lyricUrl
    private String GetSongLyrics(String lyricUrl) {

        // Try to get the contents of the webpage
        Document doc = null;
        try {
            doc = Jsoup.connect(lyricUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        Element contents = doc.getElementById("songLyricsDiv");

        // Clean up some of the text
        String lyrics = contents.text();
        lyrics = lyrics.replaceAll("\\[.*?\\]", " ");
        lyrics = lyrics.replaceAll("\\(.*?\\)", " ");
        lyrics = lyrics.replaceAll("’", "'");
        lyrics = lyrics.replaceAll("[^\\P{P}'-]+", " ");
        lyrics = lyrics.toLowerCase();
        return lyrics;
    }
}
