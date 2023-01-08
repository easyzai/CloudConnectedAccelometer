package com.awaissaikhu.cloudconnectedaccelometer;

import java.io.Serializable;

public class leaderboardModal implements Serializable {
    int rank;
    String name;
    double score;
    public leaderboardModal(int rank,String name,double score){
        this.rank=rank;
        this.name=name;
        this.score=score;
    }
}
