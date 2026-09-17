package com.example.music;

public class Statistics {
    private int listeningScore;

    public Statistics(int listeningScore) {
        this.listeningScore = listeningScore;
    }

    public int getListeningScore() {
        return listeningScore;
    }

    public void calculateStatistics() {
    }

    public String getTopSongs() {
        return "Top 5 visvairāk klausītās dziesmas";
    }
}
