package com.example.nemuni.mymusiclist.bean;

import java.text.DecimalFormat;

public class MusicMsg {

    private String music;

    private String singer;

    private String path;

    private int duration;

    private long size;

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        int first = 0,last = music.lastIndexOf(".");
        if ((first = music.indexOf("-")) == -1) {
            first = 0;
        } else {
            first++;
        }
        music = music.substring(first, last).trim();
        this.music = music;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    DecimalFormat df = null;
    public String getSizeWithMB() {
        if (df == null) {
            df = new DecimalFormat("#.0");
        }
        return df.format((double)size / 1048576) + "M";
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "MusicMsg{" +
                "music='" + music + '\'' +
                ", singer='" + singer + '\'' +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
