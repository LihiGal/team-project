package use_case.talk_to_npc;

import entity.GameStatus;

/**
 * Contains the game state information after the player
 * chooses a dialogue response.
 */
public class ChooseOptionOutputData {

    private final String message;
    private final int energy;
    private final int sanity;
    private final int academicSurvival;
    private final int money;
    private final int day;
    private final int remainingTimeBlocks;
    private final String currentLocationId;
    private final GameStatus status;

    public ChooseOptionOutputData(String message, int energy, int sanity,
                                  int academicSurvival, int money, int day, int remainingTimeBlocks,
                                  String currentLocationId, GameStatus status) {
        this.message = message;
        this.energy = energy;
        this.sanity = sanity;
        this.academicSurvival = academicSurvival;
        this.money = money;
        this.day = day;
        this.remainingTimeBlocks = remainingTimeBlocks;
        this.currentLocationId = currentLocationId;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getEnergy() {
        return energy;
    }

    public int getSanity() {
        return sanity;
    }

    public int getAcademicSurvival() {
        return academicSurvival;
    }

    public int getMoney() {
        return money;
    }

    public int getDay(){
        return day;
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

    /** True if a stat hit 0 because of this response. */
    public boolean isGameOver() {
        return status == GameStatus.WON || status == GameStatus.LOST;
    }
}
