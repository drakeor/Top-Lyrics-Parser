package com.drakeor.lyricparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class LyricApp {

    private void FilterWords(String lyric, HashMap<String, Integer> listOfWords) {
        String[] words = lyric.split(" ");
        for(String word : words) {
            word = word.trim();
            if(!word.isEmpty()) {
                Integer currentWordCount = listOfWords.get(word);
                if (currentWordCount == null) {
                    listOfWords.put(word, 1);
                } else {
                    listOfWords.put(word, currentWordCount + 1);
                }
            }
        }
    }

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
        Elements contents = doc.getElementsByClass("tracklist");
        for(Element content : contents) {

            // Only grab the last cell of each table to prevent duplicate links
            Elements correctRows = content.getElementsByClass("td-last");
            for(Element correctRow : correctRows) {

                // Scrape the links and add them to the hashset
                Elements links = correctRow.getElementsByTag("a");
                for (Element link : links) {
                    String linkHref = link.attr("href");
                    songList.add(linkHref);
                }
            }
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
