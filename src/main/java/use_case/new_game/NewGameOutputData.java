package use_case.new_game;

/**
 * What the presenter needs to show a freshly started game. Plain values
 * only, so the GameState entity never leaks into the interface adapter layer.
 */
public class NewGameOutputData {

    private final int day;
    private final int totalDays;
    private final int energy;
    private final int sanity;
    private final int academicSurvival;
    private final int money;
    private final int remainingTimeBlocks;

    public NewGameOutputData(int day, int totalDays, int energy, int sanity,
                             int academicSurvival, int money, int remainingTimeBlocks) {
        this.day = day;
        this.totalDays = totalDays;
        this.energy = energy;
        this.sanity = sanity;
        this.academicSurvival = academicSurvival;
        this.money = money;
        this.remainingTimeBlocks = remainingTimeBlocks;
    }

    public int getDay() {
        return day;
    }

    public int getTotalDays() {
        return totalDays;
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

    public int getRemainingTimeBlocks(){
        return remainingTimeBlocks;
    }
}
