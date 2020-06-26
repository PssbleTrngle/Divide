package com.possible_triangle.divide.data;

public class TeamData {

    private int rank;

    public TeamData(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        if(rank < 0) throw new IllegalArgumentException("Team rank cannot be sub-zero");
        this.rank = rank;
    }
}
