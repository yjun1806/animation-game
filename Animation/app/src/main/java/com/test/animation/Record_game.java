package com.test.animation;

public class Record_game {
    String time;
    String name;

    Record_game(){

    }

    Record_game(String name, String time){
        this.name = name;
        this.time = time;
    }

    public String getTime(){
        return this.time;
    }

}
