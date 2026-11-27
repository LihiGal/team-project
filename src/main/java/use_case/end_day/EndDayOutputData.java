package use_case.end_day;

import entity.GameStatus;

public class EndDayOutputData {

    private final int day;
    private final int energy;
    private final int sanity;
    private final int academicSurvival;
    private final int money;
    private final int remainingTimeBlocks;
    private final String currentLocationId;
    private final GameStatus status;

    public EndDayOutputData(int day, int energy, int sanity, int academicSurvival, int money, int remainingTimeBlocks, String currentLocationId, GameStatus status){
        this.day = day;
        this.energy = energy;
        this.sanity = sanity;
        this.academicSurvival = academicSurvival;
        this.money = money;
        this.remainingTimeBlocks = remainingTimeBlocks;
        this.currentLocationId = currentLocationId;
        this.status = status;
    }

    public int getDay(){
        return day;
    }

    public int getEnergy(){
        return energy;
    }

    public int getSanity(){
        return sanity;
    }

    public int getAcademicSurvival() {
        return academicSurvival;
    }

    public int getMoney(){
        return money;
    }

    public int getRemainingTimeBlocks(){
        return remainingTimeBlocks;
    }

    public String getCurrentLocationId(){
        return currentLocationId;
    }

    public GameStatus getStatus(){
        return status;
    }
}
