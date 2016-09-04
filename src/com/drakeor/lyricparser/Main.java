package com.drakeor.lyricparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedHashSet;

public class Main {

    public static void main(String[] args) {
        LyricApp lyricApp = new LyricApp();
        lyricApp.Run();
    }
}
